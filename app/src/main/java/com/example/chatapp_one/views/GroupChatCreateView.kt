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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.chatapp_one.data.users.User
import com.example.chatapp_one.viewModels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupChatCreateView(
    currentUserId: String,
    userFriends: List<User>, // List of friends → pass from UserViewModel.userFriends
    chatViewModel: ChatViewModel,
    navController: NavController
) {
    var selectedFriendIds by remember { mutableStateOf(setOf<String>()) }
    var groupName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                title = { Text("Create Group Chat") }
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(6.dp)
        ) {
            // Group name input
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Friends:", style = MaterialTheme.typography.subtitle1)

            Spacer(modifier = Modifier.height(8.dp))

            // Friends list
            LazyColumn {
                items(userFriends) { friend ->
                    val isSelected = selectedFriendIds.contains(friend.userId)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .background(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable {
                                selectedFriendIds = if (isSelected) {
                                    selectedFriendIds - friend.userId
                                } else {
                                    selectedFriendIds + friend.userId
                                }
                            }
                            .padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedFriendIds = if (checked) {
                                        selectedFriendIds + friend.userId
                                    } else {
                                        selectedFriendIds - friend.userId
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Round avatar image
                            val avatarResId = com.example.chatapp_one.utility.AvatarUtils.getAvatarDrawableRes(friend.avatar)

                            Image(
                                painter = painterResource(id = avatarResId),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = friend.displayName,
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Create group button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val participants = listOf(currentUserId) + selectedFriendIds.toList()
                        val chatId = chatViewModel.createGroupChat(
                            participants = participants,
                            groupName = groupName.ifBlank { "Unnamed Group" },
                            createdBy = currentUserId
                        )
                        navController.navigate(Screen.ChatScreen.routeWithArgs(chatId))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Group")
            }
        }
    }
}


