package com.example.chatapp_one.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp_one.Screen
import com.example.chatapp_one.viewModels.ChatViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun FriendsView(
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
) {
    val userFriends by userViewModel.userFriends.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentUser by userViewModel.user.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            if (userFriends.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("No friends to show.")
                    }
                }
            } else {
                items(userFriends) { friendUser ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                coroutineScope.launch {
                                    try {
                                        val chatId = chatViewModel.getOrCreatePrivateChat(
                                            userA = currentUser?.userId ?: return@launch, // fallback to early return if null
                                            userB = friendUser.userId
                                        )
                                        navController.navigate(Screen.ChatScreen.routeWithArgs(chatId))

                                    } catch (e: Exception) {
                                        // optional: show toast or log error
                                    }
                                }
                            }
                    ) {
                        Text(text = friendUser.displayName)
                        Divider()
                    }
                }
            }
        }
    }
}

