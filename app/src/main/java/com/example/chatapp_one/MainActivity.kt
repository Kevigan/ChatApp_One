package com.example.chatapp_one

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.chatapp_one.ui.theme.ChatApp_OneTheme
import com.example.chatapp_one.viewModels.ChatViewModel
import com.example.chatapp_one.viewModels.SessionViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import com.example.chatapp_one.views.ChatAppMainView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionViewModel: SessionViewModel by viewModels()
        val userViewModel: UserViewModel by viewModels()
        val chatViewModel: ChatViewModel by viewModels()

        setContent {
            val context = LocalContext.current

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("500019924424-9t13jq166fp295d9ib7ncfakps8ofkug.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val googleSignInClient = remember {
                GoogleSignIn.getClient(context, gso)
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                sessionViewModel.handleGoogleSignInResult(
                    resultData = result.data,
                    onSuccess = { userId, displayName, email ->
                        userViewModel.checkOrCreateUser(
                            userId = userId,
                            displayName = displayName,
                            email = email,
                            onUserExists = {
                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                userViewModel.loadUser(userId) // load state
                            },
                            onUserCreated = {
                                Toast.makeText(context, "User created in Firestore", Toast.LENGTH_SHORT).show()
                                userViewModel.loadUser(userId) // load state
                            },
                            onError = {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onError = {
                        Toast.makeText(context, "Google Sign-in error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                )

            }

            ChatApp_OneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                   Navigation(
                       userViewModel = userViewModel,
                       sessionViewModel = sessionViewModel,
                       chatViewModel,
                       googleSignInClient = googleSignInClient,
                       googleSignInLauncher = launcher
                   )
                }
            }
        }
    }
}



