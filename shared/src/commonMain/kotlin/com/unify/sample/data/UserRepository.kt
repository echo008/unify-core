package com.unify.sample.data

import com.unify.database.UnifyDatabase
import com.unify.database.UserEntity
import com.unify.database.insertUserEntity
import com.unify.database.getUserEntity
import com.unify.database.getAllActiveUsers
import com.unify.database.getUserByUsername
import com.unify.database.getUserByEmail
import com.unify.database.updateUserEntity
import com.unify.database.deactivateUser
import com.unify.database.getUserCount
import com.unify.network.NetworkResult
import com.unify.network.UnifyNetworkService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null
)

@Serializable
data class UpdateUserRequest(
    val displayName: String,
    val avatarUrl: String? = null
)

@Serializable
data class UserResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)

@Serializable
data class UsersResponse(
    val success: Boolean,
    val message: String,
    val users: List<User> = emptyList(),
    val total: Int = 0
)

class UserRepository(
    private val database: UnifyDatabase,
    private val networkService: UnifyNetworkService
) {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: Flow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: Flow<String?> = _error.asStateFlow()
    
    suspend fun loadUsers(): Result<List<User>> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            // First try to load from network
            val networkResult = loadUsersFromNetwork()
            if (networkResult.isSuccess) {
                val users = networkResult.getOrNull() ?: emptyList()
                _users.value = users
                // Cache users in local database
                cacheUsersLocally(users)
                return Result.success(users)
            }
            
            // Fallback to local database
            val localUsers = loadUsersFromDatabase()
            _users.value = localUsers
            Result.success(localUsers)
            
        } catch (e: Exception) {
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createUser(request: CreateUserRequest): Result<User> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            // Validate input
            if (request.username.isBlank()) {
                throw IllegalArgumentException("Username cannot be blank")
            }
            if (request.email.isBlank() || !isValidEmail(request.email)) {
                throw IllegalArgumentException("Invalid email address")
            }
            if (request.displayName.isBlank()) {
                throw IllegalArgumentException("Display name cannot be blank")
            }
            
            // Check if username or email already exists
            val existingByUsername = database.getUserByUsername(request.username)
            if (existingByUsername != null) {
                throw IllegalArgumentException("Username already exists")
            }
            
            val existingByEmail = database.getUserByEmail(request.email)
            if (existingByEmail != null) {
                throw IllegalArgumentException("Email already exists")
            }
            
            val now = System.currentTimeMillis()
            val userEntity = UserEntity(
                id = 0, // Will be auto-generated
                username = request.username,
                email = request.email,
                displayName = request.displayName,
                avatarUrl = request.avatarUrl,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
            
            // Try to create user on server first
            val networkResult = createUserOnNetwork(request)
            val userId = if (networkResult.isSuccess) {
                val networkUser = networkResult.getOrNull()
                if (networkUser != null) {
                    // Update local database with server response
                    val serverUserEntity = userEntity.copy(id = networkUser.id)
                    database.insertUserEntity(serverUserEntity)
                    networkUser.id
                } else {
                    database.insertUserEntity(userEntity)
                }
            } else {
                // Fallback to local creation
                database.insertUserEntity(userEntity)
            }
            
            val createdUser = database.getUserEntity(userId)
                ?: throw IllegalStateException("Failed to retrieve created user")
            
            val user = createdUser.toUser()
            
            // Update local state
            val currentUsers = _users.value.toMutableList()
            currentUsers.add(user)
            _users.value = currentUsers
            
            Result.success(user)
            
        } catch (e: Exception) {
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateUser(userId: Long, request: UpdateUserRequest): Result<User> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            val existingUser = database.getUserEntity(userId)
                ?: throw IllegalArgumentException("User not found")
            
            val updatedEntity = existingUser.copy(
                displayName = request.displayName,
                avatarUrl = request.avatarUrl,
                updatedAt = System.currentTimeMillis()
            )
            
            // Try to update on server first
            val networkResult = updateUserOnNetwork(userId, request)
            if (networkResult.isSuccess) {
                val networkUser = networkResult.getOrNull()
                if (networkUser != null) {
                    // Update with server response
                    database.updateUserEntity(networkUser.toUserEntity())
                } else {
                    database.updateUserEntity(updatedEntity)
                }
            } else {
                // Fallback to local update
                database.updateUserEntity(updatedEntity)
            }
            
            val updatedUser = database.getUserEntity(userId)
                ?: throw IllegalStateException("Failed to retrieve updated user")
            
            val user = updatedUser.toUser()
            
            // Update local state
            val currentUsers = _users.value.toMutableList()
            val index = currentUsers.indexOfFirst { it.id == userId }
            if (index != -1) {
                currentUsers[index] = user
                _users.value = currentUsers
            }
            
            Result.success(user)
            
        } catch (e: Exception) {
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null
            
            // Try to delete on server first
            val networkResult = deleteUserOnNetwork(userId)
            if (networkResult.isSuccess || networkResult.isFailure) {
                // Proceed with local deletion regardless of network result
                database.deactivateUser(userId)
            }
            
            // Update local state
            val currentUsers = _users.value.toMutableList()
            currentUsers.removeAll { it.id == userId }
            _users.value = currentUsers
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            _error.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getUserById(userId: Long): Result<User?> {
        return try {
            val userEntity = database.getUserEntity(userId)
            Result.success(userEntity?.toUser())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val allUsers = database.getAllActiveUsers()
            val filteredUsers = allUsers.filter { user ->
                user.username.contains(query, ignoreCase = true) ||
                user.displayName.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
            }.map { it.toUser() }
            
            Result.success(filteredUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private suspend fun loadUsersFromNetwork(): Result<List<User>> {
        return try {
            val result = networkService.get<UsersResponse>(
                url = "/api/users",
                headers = mapOf("Content-Type" to "application/json"),
                responseType = UsersResponse::class
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.success) {
                        Result.success(response.users)
                    } else {
                        Result.failure(Exception(response.message))
                    }
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception(result.error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun loadUsersFromDatabase(): List<User> {
        return database.getAllActiveUsers().map { it.toUser() }
    }
    
    private suspend fun cacheUsersLocally(users: List<User>) {
        try {
            // Clear existing users and insert new ones
            // This is a simplified approach - in production you'd want more sophisticated sync
            users.forEach { user ->
                database.insertUserEntity(user.toUserEntity())
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }
    
    private suspend fun createUserOnNetwork(request: CreateUserRequest): Result<User?> {
        return try {
            val result = networkService.post<UserResponse>(
                url = "/api/users",
                body = request,
                headers = mapOf("Content-Type" to "application/json"),
                responseType = UserResponse::class
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.success) {
                        Result.success(response.user)
                    } else {
                        Result.failure(Exception(response.message))
                    }
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception(result.error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun updateUserOnNetwork(userId: Long, request: UpdateUserRequest): Result<User?> {
        return try {
            val result = networkService.put<UserResponse>(
                url = "/api/users/$userId",
                body = request,
                headers = mapOf("Content-Type" to "application/json"),
                responseType = UserResponse::class
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.success) {
                        Result.success(response.user)
                    } else {
                        Result.failure(Exception(response.message))
                    }
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception(result.error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun deleteUserOnNetwork(userId: Long): Result<Unit> {
        return try {
            val result = networkService.delete<UserResponse>(
                url = "/api/users/$userId",
                headers = mapOf("Content-Type" to "application/json"),
                responseType = UserResponse::class
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(response.message))
                    }
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception(result.error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

// Extension functions for data conversion
private fun UserEntity.toUser(): User {
    return User(
        id = id,
        username = username,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive
    )
}
