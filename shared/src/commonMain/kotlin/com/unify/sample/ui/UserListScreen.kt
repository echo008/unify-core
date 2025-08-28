package com.unify.sample.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unify.sample.data.User
import com.unify.sample.presentation.UserIntent
import com.unify.sample.presentation.UserState
import com.unify.sample.presentation.UserViewModel
import com.unify.ui.components.UnifyButton
import com.unify.ui.components.UnifyImage
import com.unify.ui.components.UnifyInput
import com.unify.ui.components.UnifyText
import com.unify.ui.components.UnifyView
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel = koinInject(),
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val state by viewModel.stateFlow.collectAsState()
    
    // Handle effects
    LaunchedEffect(viewModel) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is com.unify.sample.presentation.UserEffect.NavigateToUserDetail -> {
                    onNavigateToDetail(effect.userId)
                }
                else -> {
                    // Handle other effects like showing snackbars
                }
            }
        }
    }
    
    UnifyView(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            UserListHeader(
                onRefresh = { viewModel.handleIntent(UserIntent.RefreshUsers) },
                onAddUser = { viewModel.handleIntent(UserIntent.ShowCreateDialog) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.handleIntent(UserIntent.SearchUsers(it)) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User List
            UserList(
                users = state.filteredUsers,
                isLoading = state.isLoading,
                onUserClick = { user ->
                    viewModel.handleIntent(UserIntent.SelectUser(user))
                    onNavigateToDetail(user.id)
                },
                onEditUser = { user ->
                    viewModel.handleIntent(UserIntent.ShowEditDialog(user))
                },
                onDeleteUser = { user ->
                    viewModel.handleIntent(UserIntent.ShowDeleteDialog(user))
                }
            )
        }
        
        // Dialogs
        if (state.showCreateDialog) {
            CreateUserDialog(
                form = state.createUserForm,
                onDismiss = { viewModel.handleIntent(UserIntent.HideCreateDialog) },
                onUsernameChange = { viewModel.handleIntent(UserIntent.UpdateCreateForm(username = it)) },
                onEmailChange = { viewModel.handleIntent(UserIntent.UpdateCreateForm(email = it)) },
                onDisplayNameChange = { viewModel.handleIntent(UserIntent.UpdateCreateForm(displayName = it)) },
                onAvatarUrlChange = { viewModel.handleIntent(UserIntent.UpdateCreateForm(avatarUrl = it)) },
                onCreateUser = { viewModel.handleIntent(UserIntent.CreateUser) },
                isLoading = state.isLoading
            )
        }
        
        if (state.showEditDialog) {
            EditUserDialog(
                user = state.selectedUser,
                form = state.editUserForm,
                onDismiss = { viewModel.handleIntent(UserIntent.HideEditDialog) },
                onDisplayNameChange = { viewModel.handleIntent(UserIntent.UpdateEditForm(displayName = it)) },
                onAvatarUrlChange = { viewModel.handleIntent(UserIntent.UpdateEditForm(avatarUrl = it)) },
                onSaveUser = { viewModel.handleIntent(UserIntent.SaveUser) },
                isLoading = state.isLoading
            )
        }
        
        if (state.showDeleteDialog) {
            DeleteUserDialog(
                user = state.selectedUser,
                onDismiss = { viewModel.handleIntent(UserIntent.HideDeleteDialog) },
                onConfirmDelete = { viewModel.handleIntent(UserIntent.ConfirmDeleteUser) },
                isLoading = state.isLoading
            )
        }
        
        // Error Snackbar
        state.error?.let { error ->
            LaunchedEffect(error) {
                // In a real app, you'd show a snackbar here
                viewModel.handleIntent(UserIntent.ClearError)
            }
        }
    }
}

@Composable
private fun UserListHeader(
    onRefresh: () -> Unit,
    onAddUser: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UnifyText(
            text = "Users",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = onAddUser,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add User",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { UnifyText("Search users...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun UserList(
    users: List<User>,
    isLoading: Boolean,
    onUserClick: (User) -> Unit,
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit
) {
    if (isLoading && users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                UnifyText(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                UserListItem(
                    user = user,
                    onClick = { onUserClick(user) },
                    onEdit = { onEditUser(user) },
                    onDelete = { onDeleteUser(user) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListItem(
    user: User,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (user.avatarUrl != null) {
                UnifyImage(
                    src = user.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            UnifyText(
                                text = user.displayName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                UnifyText(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                UnifyText(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                UnifyText(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
