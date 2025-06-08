package com.example.chatapp_one.views

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatapp_one.R
import com.example.chatapp_one.Screen
import com.example.chatapp_one.data.users.chats.Chat
import com.example.chatapp_one.viewModels.ChatViewModel
import com.example.chatapp_one.viewModels.SessionViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.launch

@Composable
fun ChatAppMainView(
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
    googleSignInClient: GoogleSignInClient,
    googleSignInLauncher: ActivityResultLauncher<Intent>,
) {
    val currentUser by sessionViewModel.currentUser.collectAsState() //auth
    val user by userViewModel.user.collectAsState() //firebase
    val currentChatUser by userViewModel.user.collectAsState()

    val showLoginDialog = remember { mutableStateOf(currentUser == null) }
    val showSignOutDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Watch for auth changes to control login dialog
    LaunchedEffect(currentUser) {
        showLoginDialog.value = currentUser == null

        val uid = currentUser?.uid
        if (uid != null) {
            userViewModel.loadUser(uid)
        }
    }

    if (showLoginDialog.value) {
        LoginDialog(
            userViewModel = userViewModel,
            onLoginSuccess = { showLoginDialog.value = false },
            onDismiss = { },
            sessionViewModel = sessionViewModel,
            googleSignInClient = googleSignInClient,
            launcher = googleSignInLauncher
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val showAddFriendDialog = remember { mutableStateOf(false) }

    if (showAddFriendDialog.value) {
        AddFriendDialog(
            onAddFriend = { email ->
                coroutineScope.launch {
                    val currentUserId = user?.userId
                    if (currentUserId == null) {
                        Toast.makeText(context, "Error: user not loaded", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    userViewModel.addFriendByEmail(
                        currentUserId = currentUserId,
                        friendEmail = email,
                        onSuccess = {
                            Toast.makeText(context, "Friend added!", Toast.LENGTH_SHORT).show()
                            showAddFriendDialog.value = false
                        },
                        onUserNotFound = {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(context, "Error adding friend: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
            ,
            onDismiss = { showAddFriendDialog.value = false }
        )

    }

    Scaffold(
        topBar = { ChatAppTopBar(
            displayName = user?.displayName ?: "Loading...",
            onSignOutClicked = { showSignOutDialog.value = true }
        ) },
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
        ChatListContent(
            currentUserId = currentChatUser?.userId ?: "",
            chatViewModel = chatViewModel,
            navController = navController,
            contentPadding = innerPadding  // pass it down!
        )
    }
    //Show signout dialog
    if (showSignOutDialog.value) {
        SignOutDialog(
            onConfirmSignOut = {
                sessionViewModel.signOut()
                showSignOutDialog.value = false
            },
            onDismiss = { showSignOutDialog.value = false }
        )
    }

}

@Composable
fun ChatAppTopBar(displayName: String, onSignOutClicked: () -> Unit) {
    TopAppBar(
        title = { Text("ChatApp - $displayName") },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        actions = {
            IconButton(onClick = onSignOutClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_logout_24),
                    contentDescription = "Sign Out",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
fun ChatListContent(
    currentUserId: String,
    chatViewModel: ChatViewModel,
    navController: NavController,
    contentPadding: PaddingValues = PaddingValues()
) {
    val chats by chatViewModel.chats.collectAsState()

    // Load chats when this view appears
    LaunchedEffect(currentUserId) {
        chatViewModel.loadChatsForUser(currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (chats.isEmpty()) {
            // No chats → show message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No chat started yet")
            }
        } else {
            // Show chat list
            LazyColumn {
                items(chats) { chat ->
                    ChatListItem(
                        chat = chat,
                        currentUserId = currentUserId,
                        onClick = {
                            navController.navigate(Screen.ChatScreen.routeWithArgs(chat.chatId))
                        }
                    )
                    Divider()
                }
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
                if (email.isNotBlank()) {
                    onAddFriend(email.trim())
                }
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

@Composable
fun SignOutDialog(
    onConfirmSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Out") },
        text = { Text("Are you sure you want to sign out?") },
        confirmButton = {
            TextButton(onClick = onConfirmSignOut) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

@Composable
fun ChatListItem(
    chat: Chat,
    currentUserId: String,
    onClick: () -> Unit
) {
    val chatTitle = if (chat.groupChat) {
        chat.groupName ?: "Group Chat"
    } else {
        val otherUserId = chat.participants.firstOrNull { it != currentUserId }
        chat.participantDisplayNames[otherUserId] ?: "Private Chat"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = chatTitle,
            style = MaterialTheme.typography.subtitle1
        )
        if (chat.lastMessage.isNotBlank()) {
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }
    }
}






