package com.example.chatapp_one.views

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.chatapp_one.R
import com.example.chatapp_one.viewModels.SessionViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun LoginDialog(
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel,
    onLoginSuccess: () -> Unit,
    onDismiss: () -> Unit,
    googleSignInClient: GoogleSignInClient,
    launcher: ActivityResultLauncher<Intent>
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isRegisterMode) "Register Account" else "Login Required",
                color = MaterialTheme.colors.onSurface
            )
        },
        text = {
            Column {
                ThemedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )

                ThemedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true
                )

                if (isRegisterMode) {
                    ThemedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        isPassword = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (email.isBlank() || password.isBlank() || (isRegisterMode && confirmPassword.isBlank())) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                if (isRegisterMode) {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    sessionViewModel.register(
                        email.trim(), password,
                        onSuccess = {
                            Toast.makeText(context, "Registered and logged in", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        onFailure = {
                            Toast.makeText(context, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    sessionViewModel.login(
                        email.trim(),
                        password,
                        onSuccess = { userId, displayName, email ->
                            userViewModel.checkOrCreateUser(
                                userId = userId,
                                displayName = displayName,
                                email = email,
                                onUserExists = {
                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                    userViewModel.loadUser(userId)
                                    onLoginSuccess()
                                },
                                onUserCreated = {
                                    Toast.makeText(context, "User created in Firestore", Toast.LENGTH_SHORT).show()
                                    userViewModel.loadUser(userId)
                                    onLoginSuccess()
                                },
                                onError = {
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onFailure = {
                            Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )

                }
            }) {
                Text(text = if (isRegisterMode) "Register" else "Login", color = MaterialTheme.colors.onSurface)
            }
        }
        ,
        dismissButton = {
            Column {
                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        if (isRegisterMode) "Switch to Login" else "Switch to Register",
                        color = MaterialTheme.colors.onSurface
                    )
                }

                TextButton(onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }) {
                    Text("Sign in with Google", color = MaterialTheme.colors.onSurface)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colors.onSurface)
                }
            }
        }
    )
}

@Composable
fun ThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    var showPassword by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = MaterialTheme.colors.onSurface
            )
        },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword && !showPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                val icon = if (showPassword)
                    R.drawable.baseline_visibility_off_24
                else
                    R.drawable.baseline_visibility_24

                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = MaterialTheme.colors.onSurface // ✅ Theme-aware icon
                    )
                }
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.colors.onSurface,
            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colors.primary,
            focusedLabelColor = MaterialTheme.colors.primary,
            unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    )
}


