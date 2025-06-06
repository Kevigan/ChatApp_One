package com.example.chatapp_one.views

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Divider
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatapp_one.R
import com.example.chatapp_one.Screen
import com.example.chatapp_one.viewModels.SessionViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun ChatAppMainView(
    sessionViewModel: SessionViewModel,
    navController: NavController,
    googleSignInClient: GoogleSignInClient,
    googleSignInLauncher: ActivityResultLauncher<Intent>,
) {
    val currentUser by sessionViewModel.currentUser.collectAsState()
    val showLoginDialog = remember { mutableStateOf(currentUser == null) }

    // Watch for auth changes to control login dialog
    LaunchedEffect(currentUser) {
        showLoginDialog.value = currentUser == null
    }

    if (showLoginDialog.value) {
        LoginDialog(
            onLoginSuccess = { showLoginDialog.value = false },
            onDismiss = { },
            sessionViewModel = sessionViewModel,
            googleSignInClient = googleSignInClient,
            launcher = googleSignInLauncher
        )
    }

    val showAddFriendDialog = remember { mutableStateOf(false) }

    if (showAddFriendDialog.value) {
        AddFriendDialog(
            onAddFriend = { email ->
                // TODO: handle adding friend (Firebase lookup etc.)
                showAddFriendDialog.value = false
            },
            onDismiss = { showAddFriendDialog.value = false }
        )
    }

    Scaffold(
        topBar = { ChatAppTopBar() },
        bottomBar = {
            ChatAppBottomBar(
                navController,
                onAddFriendClicked = { showAddFriendDialog.value = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Start new chat */ }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        ChatListContent(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ChatAppTopBar() {
    TopAppBar(
        title = { Text("ChatApp") },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White
    )
}

@Composable
fun ChatAppBottomBar(
    navController: NavController,
    onAddFriendClicked: () -> Unit,
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding().height(70.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomBarButton("Add Friend", R.drawable.baseline_person_add_24, onClick = onAddFriendClicked)
            BottomBarButton("Friends", R.drawable.baseline_people_24, onClick = {navController.navigate(Screen.FriendsScreen.route)})
            BottomBarButton("Settings", R.drawable.baseline_settings_24, onClick = { navController.navigate(Screen.SettingsScreen.route) })
            BottomBarButton("Account", R.drawable.baseline_group_add_24, onClick = { navController.navigate(Screen.AccountScreen.route) })
        }
    }
}


@Composable
fun BottomBarButton(label: String, @DrawableRes iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(text = label, fontSize = 10.sp)
    }
}

@Composable
fun ChatListContent(modifier: Modifier = Modifier) {
    val sampleChats = remember {
        listOf("Alice", "Bob", "Charlie", "Dev Group", "Study Buddies")
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        LazyColumn {
            items(sampleChats, key = { it }) { chatName ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Text(
                        text = chatName,
                        color =  MaterialTheme.colors.primary,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Last message preview...",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
                Divider()
            }
        }
    }

}

@Composable
fun AddFriendDialog(
    onAddFriend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend") },
        text = {
            Column {
                Text("Enter your friend's email address:")
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAddFriend(email.trim())
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}






