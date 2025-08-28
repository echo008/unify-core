package com.unify.sample.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.sample.data.User
import com.unify.sample.presentation.UserIntent
import com.unify.sample.presentation.UserViewModel
import com.unify.ui.components.UnifyButton
import com.unify.ui.components.UnifyImage
import com.unify.ui.components.UnifyText
import com.unify.ui.components.UnifyView
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = koinInject()
) {
    val state by viewModel.stateFlow.collectAsState()
    
    // Load user details
    LaunchedEffect(userId) {
        viewModel.handleIntent(UserIntent.LoadUsers())
    }
    
    val user = state.users.find { it.id == userId }
    
    if (user == null) {
        // User not found
        UnifyView(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                UnifyText(
                    text = "User not found",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                UnifyButton(
                    text = "Go Back",
                    onClick = onNavigateBack
                )
            }
        }
        return
    }
    
    UnifyView(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = { UnifyText("User Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleIntent(UserIntent.ShowEditDialog(user)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit User",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.handleIntent(UserIntent.ShowDeleteDialog(user)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // User Profile Card
                UserProfileCard(user = user)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // User Information Card
                UserInformationCard(user = user)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Account Status Card
                AccountStatusCard(user = user)
            }
        }
        
        // Dialogs
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
                onConfirmDelete = { 
                    viewModel.handleIntent(UserIntent.ConfirmDeleteUser)
                    onNavigateBack() // Navigate back after deletion
                },
                isLoading = state.isLoading
            )
        }
    }
}

@Composable
private fun UserProfileCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            if (user.avatarUrl != null) {
                UnifyImage(
                    src = user.avatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
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
                                text = user.displayName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display Name
            UnifyText(
                text = user.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Username
            UnifyText(
                text = "@${user.username}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status Badge
            Surface(
                color = if (user.isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                UnifyText(
                    text = if (user.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (user.isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun UserInformationCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            UnifyText(
                text = "Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email
            InformationRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = user.email
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Username
            InformationRow(
                icon = Icons.Default.Person,
                label = "Username",
                value = user.username
            )
        }
    }
}

@Composable
private fun AccountStatusCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            UnifyText(
                text = "Account Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Created Date
            InformationRow(
                label = "Created",
                value = formatDate(user.createdAt)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Last Updated
            InformationRow(
                label = "Last Updated",
                value = formatDate(user.updatedAt)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // User ID
            InformationRow(
                label = "User ID",
                value = user.id.toString()
            )
        }
    }
}

@Composable
private fun InformationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            UnifyText(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            UnifyText(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
}
