package com.unify.sample.presentation

import com.unify.mvi.UnifyViewModel
import com.unify.mvi.State
import com.unify.mvi.Intent
import com.unify.mvi.Effect
import com.unify.mvi.StateReducer
import com.unify.sample.data.User
import com.unify.sample.data.UserRepository
import com.unify.sample.data.CreateUserRequest
import com.unify.sample.data.UpdateUserRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// State
data class UserState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filteredUsers: List<User> = emptyList(),
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val createUserForm: CreateUserForm = CreateUserForm(),
    val editUserForm: EditUserForm = EditUserForm()
) : State

data class CreateUserForm(
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val isValid: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

data class EditUserForm(
    val displayName: String = "",
    val avatarUrl: String = "",
    val isValid: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

// Intents
sealed class UserIntent : Intent {
    object LoadUsers : UserIntent()
    object RefreshUsers : UserIntent()
    data class SearchUsers(val query: String) : UserIntent()
    data class SelectUser(val user: User) : UserIntent()
    object ClearSelection : UserIntent()
    
    // Create User
    object ShowCreateDialog : UserIntent()
    object HideCreateDialog : UserIntent()
    data class UpdateCreateForm(
        val username: String? = null,
        val email: String? = null,
        val displayName: String? = null,
        val avatarUrl: String? = null
    ) : UserIntent()
    object CreateUser : UserIntent()
    
    // Edit User
    data class ShowEditDialog(val user: User) : UserIntent()
    object HideEditDialog : UserIntent()
    data class UpdateEditForm(
        val displayName: String? = null,
        val avatarUrl: String? = null
    ) : UserIntent()
    object SaveUser : UserIntent()
    
    // Delete User
    data class ShowDeleteDialog(val user: User) : UserIntent()
    object HideDeleteDialog : UserIntent()
    object ConfirmDeleteUser : UserIntent()
    
    object ClearError : UserIntent()
}

// Effects
sealed class UserEffect : Effect {
    data class ShowMessage(val message: String) : UserEffect()
    data class ShowError(val error: String) : UserEffect()
    object UserCreated : UserEffect()
    object UserUpdated : UserEffect()
    object UserDeleted : UserEffect()
    data class NavigateToUserDetail(val userId: Long) : UserEffect()
}

class UserViewModel(
    private val userRepository: UserRepository
) : UnifyViewModel<UserState, UserIntent, UserEffect>() {
    
    override fun createInitialState(): UserState = UserState()
    
    override fun createReducer(): StateReducer<UserState, UserIntent> = { state, intent ->
        when (intent) {
            is UserIntent.LoadUsers -> state.copy(isLoading = true, error = null)
            is UserIntent.RefreshUsers -> state.copy(isLoading = true, error = null)
            
            is UserIntent.SearchUsers -> {
                val filteredUsers = if (intent.query.isBlank()) {
                    state.users
                } else {
                    state.users.filter { user ->
                        user.username.contains(intent.query, ignoreCase = true) ||
                        user.displayName.contains(intent.query, ignoreCase = true) ||
                        user.email.contains(intent.query, ignoreCase = true)
                    }
                }
                state.copy(
                    searchQuery = intent.query,
                    filteredUsers = filteredUsers
                )
            }
            
            is UserIntent.SelectUser -> state.copy(selectedUser = intent.user)
            is UserIntent.ClearSelection -> state.copy(selectedUser = null)
            
            // Create User Dialog
            is UserIntent.ShowCreateDialog -> state.copy(
                showCreateDialog = true,
                createUserForm = CreateUserForm()
            )
            is UserIntent.HideCreateDialog -> state.copy(
                showCreateDialog = false,
                createUserForm = CreateUserForm()
            )
            
            is UserIntent.UpdateCreateForm -> {
                val currentForm = state.createUserForm
                val updatedForm = currentForm.copy(
                    username = intent.username ?: currentForm.username,
                    email = intent.email ?: currentForm.email,
                    displayName = intent.displayName ?: currentForm.displayName,
                    avatarUrl = intent.avatarUrl ?: currentForm.avatarUrl
                )
                val validatedForm = validateCreateForm(updatedForm)
                state.copy(createUserForm = validatedForm)
            }
            
            is UserIntent.CreateUser -> state.copy(isLoading = true, error = null)
            
            // Edit User Dialog
            is UserIntent.ShowEditDialog -> state.copy(
                showEditDialog = true,
                selectedUser = intent.user,
                editUserForm = EditUserForm(
                    displayName = intent.user.displayName,
                    avatarUrl = intent.user.avatarUrl ?: ""
                )
            )
            is UserIntent.HideEditDialog -> state.copy(
                showEditDialog = false,
                editUserForm = EditUserForm()
            )
            
            is UserIntent.UpdateEditForm -> {
                val currentForm = state.editUserForm
                val updatedForm = currentForm.copy(
                    displayName = intent.displayName ?: currentForm.displayName,
                    avatarUrl = intent.avatarUrl ?: currentForm.avatarUrl
                )
                val validatedForm = validateEditForm(updatedForm)
                state.copy(editUserForm = validatedForm)
            }
            
            is UserIntent.SaveUser -> state.copy(isLoading = true, error = null)
            
            // Delete User Dialog
            is UserIntent.ShowDeleteDialog -> state.copy(
                showDeleteDialog = true,
                selectedUser = intent.user
            )
            is UserIntent.HideDeleteDialog -> state.copy(showDeleteDialog = false)
            is UserIntent.ConfirmDeleteUser -> state.copy(isLoading = true, error = null)
            
            is UserIntent.ClearError -> state.copy(error = null)
        }
    }
    
    override fun handleIntent(intent: UserIntent) {
        super.handleIntent(intent)
        
        when (intent) {
            is UserIntent.LoadUsers -> loadUsers()
            is UserIntent.RefreshUsers -> refreshUsers()
            is UserIntent.CreateUser -> createUser()
            is UserIntent.SaveUser -> saveUser()
            is UserIntent.ConfirmDeleteUser -> deleteUser()
            else -> {
                // Other intents are handled by the reducer
            }
        }
    }
    
    init {
        // Observe repository state changes
        viewModelScope.launch {
            userRepository.users.collectLatest { users ->
                updateState { currentState ->
                    val filteredUsers = if (currentState.searchQuery.isBlank()) {
                        users
                    } else {
                        users.filter { user ->
                            user.username.contains(currentState.searchQuery, ignoreCase = true) ||
                            user.displayName.contains(currentState.searchQuery, ignoreCase = true) ||
                            user.email.contains(currentState.searchQuery, ignoreCase = true)
                        }
                    }
                    currentState.copy(
                        users = users,
                        filteredUsers = filteredUsers,
                        isLoading = false
                    )
                }
            }
        }
        
        viewModelScope.launch {
            userRepository.isLoading.collectLatest { isLoading ->
                updateState { it.copy(isLoading = isLoading) }
            }
        }
        
        viewModelScope.launch {
            userRepository.error.collectLatest { error ->
                updateState { it.copy(error = error, isLoading = false) }
                error?.let {
                    sendEffect(UserEffect.ShowError(it))
                }
            }
        }
        
        // Load users on initialization
        loadUsers()
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            val result = userRepository.loadUsers()
            if (result.isFailure) {
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun refreshUsers() {
        viewModelScope.launch {
            val result = userRepository.loadUsers()
            if (result.isSuccess) {
                sendEffect(UserEffect.ShowMessage("Users refreshed successfully"))
            } else {
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun createUser() {
        val currentState = getCurrentState()
        val form = currentState.createUserForm
        
        if (!form.isValid) {
            updateState { it.copy(error = "Please fix form errors") }
            return
        }
        
        viewModelScope.launch {
            val request = CreateUserRequest(
                username = form.username,
                email = form.email,
                displayName = form.displayName,
                avatarUrl = form.avatarUrl.takeIf { it.isNotBlank() }
            )
            
            val result = userRepository.createUser(request)
            if (result.isSuccess) {
                updateState { 
                    it.copy(
                        isLoading = false,
                        showCreateDialog = false,
                        createUserForm = CreateUserForm()
                    )
                }
                sendEffect(UserEffect.UserCreated)
                sendEffect(UserEffect.ShowMessage("User created successfully"))
            } else {
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun saveUser() {
        val currentState = getCurrentState()
        val form = currentState.editUserForm
        val selectedUser = currentState.selectedUser
        
        if (selectedUser == null) {
            updateState { it.copy(error = "No user selected") }
            return
        }
        
        if (!form.isValid) {
            updateState { it.copy(error = "Please fix form errors") }
            return
        }
        
        viewModelScope.launch {
            val request = UpdateUserRequest(
                displayName = form.displayName,
                avatarUrl = form.avatarUrl.takeIf { it.isNotBlank() }
            )
            
            val result = userRepository.updateUser(selectedUser.id, request)
            if (result.isSuccess) {
                updateState { 
                    it.copy(
                        isLoading = false,
                        showEditDialog = false,
                        editUserForm = EditUserForm()
                    )
                }
                sendEffect(UserEffect.UserUpdated)
                sendEffect(UserEffect.ShowMessage("User updated successfully"))
            } else {
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun deleteUser() {
        val currentState = getCurrentState()
        val selectedUser = currentState.selectedUser
        
        if (selectedUser == null) {
            updateState { it.copy(error = "No user selected") }
            return
        }
        
        viewModelScope.launch {
            val result = userRepository.deleteUser(selectedUser.id)
            if (result.isSuccess) {
                updateState { 
                    it.copy(
                        isLoading = false,
                        showDeleteDialog = false,
                        selectedUser = null
                    )
                }
                sendEffect(UserEffect.UserDeleted)
                sendEffect(UserEffect.ShowMessage("User deleted successfully"))
            } else {
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun validateCreateForm(form: CreateUserForm): CreateUserForm {
        val errors = mutableMapOf<String, String>()
        
        if (form.username.isBlank()) {
            errors["username"] = "Username is required"
        } else if (form.username.length < 3) {
            errors["username"] = "Username must be at least 3 characters"
        }
        
        if (form.email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!isValidEmail(form.email)) {
            errors["email"] = "Invalid email format"
        }
        
        if (form.displayName.isBlank()) {
            errors["displayName"] = "Display name is required"
        }
        
        if (form.avatarUrl.isNotBlank() && !isValidUrl(form.avatarUrl)) {
            errors["avatarUrl"] = "Invalid URL format"
        }
        
        return form.copy(
            errors = errors,
            isValid = errors.isEmpty()
        )
    }
    
    private fun validateEditForm(form: EditUserForm): EditUserForm {
        val errors = mutableMapOf<String, String>()
        
        if (form.displayName.isBlank()) {
            errors["displayName"] = "Display name is required"
        }
        
        if (form.avatarUrl.isNotBlank() && !isValidUrl(form.avatarUrl)) {
            errors["avatarUrl"] = "Invalid URL format"
        }
        
        return form.copy(
            errors = errors,
            isValid = errors.isEmpty()
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
    
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
