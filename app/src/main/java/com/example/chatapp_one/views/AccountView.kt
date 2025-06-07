package com.example.chatapp_one.views

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp_one.Screen
import com.example.chatapp_one.viewModels.SessionViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

@Composable
fun AccountView(
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val currentUser by sessionViewModel.currentUser.collectAsState()

    val showDeleteDialog = remember { mutableStateOf(false) }
    val showReauthDialog = remember { mutableStateOf(false) }
    val showChangePasswordDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            androidx.compose.material.TopAppBar(
                title = { Text("Account") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Account",
                color = Color.Black,
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Change password
            Button(
                onClick = { showChangePasswordDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Text("Change Password")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete Account
            Button(
                onClick = { showDeleteDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                )
            ) {
                Text(
                    text = "Delete Account",
                    color = MaterialTheme.colors.onError
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete Confirmation
            if (showDeleteDialog.value) {
                DeleteAccountDialog(
                    onConfirm = {
                        val firebaseUser = sessionViewModel.currentUser.value
                        val userId = firebaseUser?.uid

                        if (userId != null) {
                            userViewModel.deleteUser(
                                userId = userId,
                                onSuccess = {
                                    sessionViewModel.deleteUser(
                                        onSuccess = {
                                            Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                            navController.navigate(Screen.MainScreen.route) {
                                                popUpTo(Screen.MainScreen.route) { inclusive = true }
                                            }
                                        },
                                        onFailure = { e ->
                                            Toast.makeText(context, "Failed to delete Auth user: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onFailure = { e ->
                                    Toast.makeText(context, "Failed to delete Firestore user: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "No user found", Toast.LENGTH_SHORT).show()
                        }

                        showDeleteDialog.value = false
                    },
                    onDismiss = { showDeleteDialog.value = false }
                )

            }

            // Re-authentication Dialog
            if (showReauthDialog.value) {
                currentUser?.email?.let { email ->
                    ReAuthDialog(
                        email = email,
                        onConfirm = { password ->
                            sessionViewModel.reauthenticate(
                                email = email,
                                password = password,
                                onSuccess = {
                                    /* sessionViewModel.deleteAccountWithTasks(...) */
                                    Toast.makeText(context, "Re-auth successful (delete not implemented)", Toast.LENGTH_SHORT).show()
                                    showReauthDialog.value = false
                                    navController.navigate(Screen.MainScreen.route) {
                                        popUpTo(Screen.MainScreen.route) { inclusive = true }
                                    }
                                },
                                onFailure = {
                                    showReauthDialog.value = false
                                    Toast.makeText(context, "Re-auth failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onDismiss = { showReauthDialog.value = false }
                    )
                }
            }

            // Change Password Dialog
            if (showChangePasswordDialog.value) {
                currentUser?.email?.let { email ->
                    ChangePasswordDialog(
                        email = email,
                        onChange = { old, new ->
                            sessionViewModel.changePassword(
                                email = email,
                                currentPassword = old,
                                newPassword = new,
                                onSuccess = {
                                    Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                                    showChangePasswordDialog.value = false
                                },
                                onFailure = {
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onDismiss = { showChangePasswordDialog.value = false }
                    )
                }
            }
        }
    }
}


@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = { Text("This will permanently delete your account and data. Continue?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes, delete", color = MaterialTheme.colors.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun ReAuthDialog(
    email: String,
    onConfirm: (password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Re-authenticate") },
        text = {
            Column {
                Text("Please enter your password to continue.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    email: String,
    onChange: (oldPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (new != confirm) {
                    Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                } else {
                    onChange(current, new)
                }
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}