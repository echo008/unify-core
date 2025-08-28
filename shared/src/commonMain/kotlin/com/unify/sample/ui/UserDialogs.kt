package com.unify.sample.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.unify.sample.data.User
import com.unify.sample.presentation.CreateUserForm
import com.unify.sample.presentation.EditUserForm
import com.unify.ui.components.UnifyButton
import com.unify.ui.components.UnifyInput
import com.unify.ui.components.UnifyText

@Composable
fun CreateUserDialog(
    form: CreateUserForm,
    onDismiss: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit,
    onCreateUser: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                UnifyText(
                    text = "Create New User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Username Field
                UnifyInput(
                    value = form.username,
                    onValueChange = onUsernameChange,
                    placeholder = "Username",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                form.errors["username"]?.let { error ->
                    UnifyText(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                UnifyInput(
                    value = form.email,
                    onValueChange = onEmailChange,
                    placeholder = "Email",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                form.errors["email"]?.let { error ->
                    UnifyText(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Display Name Field
                UnifyInput(
                    value = form.displayName,
                    onValueChange = onDisplayNameChange,
                    placeholder = "Display Name",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                form.errors["displayName"]?.let { error ->
                    UnifyText(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Avatar URL Field
                UnifyInput(
                    value = form.avatarUrl,
                    onValueChange = onAvatarUrlChange,
                    placeholder = "Avatar URL (optional)",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                form.errors["avatarUrl"]?.let { error ->
                    UnifyText(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        UnifyText("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onCreateUser,
                        enabled = !isLoading && form.isValid,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            UnifyText("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: User?,
    form: EditUserForm,
    onDismiss: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit,
    onSaveUser: () -> Unit,
    isLoading: Boolean
) {
    if (user == null) return
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                UnifyText(
                    text = "Edit User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // User Info
                UnifyText(
                    text = "@${user.username} • ${user.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Display Name Field
                UnifyInput(
                    value = form.displayName,
                    onValueChange = onDisplayNameChange,
                    placeholder = "Display Name",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                form.errors["displayName"]?.let { error ->
                    UnifyText(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Avatar URL Field
                UnifyInput(
                    value = form.avatarUrl,
                    onValueChange = onAvatarUrlChange,
                    placeholder = "Avatar URL (optional)",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                form.errors["avatarUrl"]?.let { error ->
                    UnifyText(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        UnifyText("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onSaveUser,
                        enabled = !isLoading && form.isValid,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            UnifyText("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteUserDialog(
    user: User?,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    isLoading: Boolean
) {
    if (user == null) return
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                UnifyText(
                    text = "Delete User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirmation Message
                UnifyText(
                    text = "Are you sure you want to delete this user?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // User Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        UnifyText(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        UnifyText(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        UnifyText(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                UnifyText(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        UnifyText("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onConfirmDelete,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            UnifyText("Delete")
                        }
                    }
                }
            }
        }
    }
}
