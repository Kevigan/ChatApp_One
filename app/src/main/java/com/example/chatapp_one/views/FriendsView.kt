package com.example.chatapp_one.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp_one.R
import com.example.chatapp_one.Screen
import com.example.chatapp_one.utility.AvatarUtils
import com.example.chatapp_one.viewModels.ChatViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun FriendsView(
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
) {
    val userFriends by userViewModel.friends.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentUser by userViewModel.user.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                title = { Text("Friends") }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.background_0),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

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

                    val avatarResId = AvatarUtils.getAvatarDrawableRes(friendUser.avatar)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .background(Color.LightGray.copy(alpha = 0.4f), shape = MaterialTheme.shapes.medium)
                            .clickable {
                                coroutineScope.launch {
                                    try {
                                        val chatId = chatViewModel.getOrCreatePrivateChat(
                                            userA = currentUser?.userId ?: return@launch,
                                            userB = friendUser.userId
                                        )
                                        navController.navigate(Screen.ChatScreen.routeWithArgs(chatId))
                                    } catch (e: Exception) {
                                        // optional: show toast or log error
                                    }
                                }
                            }
                            .padding(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Round avatar image
                            Image(
                                painter = painterResource(id = avatarResId),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Friend display name
                            Text(
                                text = friendUser.displayName,
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                    }
                }
            }
        }
    }
}
