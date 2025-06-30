package com.example.chatapp_one.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.chatapp_one.R
import com.example.chatapp_one.utility.formatTimestamp
import com.example.chatapp_one.viewModels.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.drawBehind
import com.example.chatapp_one.utility.AvatarUtils
import com.example.chatapp_one.viewModels.UserViewModel


@Composable
fun ChatView(
    chatId: String,
    currentUserId: String,
    chatViewModel: ChatViewModel
) {
    val chat by chatViewModel.chat.collectAsState()
    val messageFlow = remember(chatId) { chatViewModel.getMessagesFlow(chatId) }
    val messages by messageFlow.collectAsState(initial = emptyList())

    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }

    val imageBitmap = ImageBitmap.imageResource(id = R.drawable.background_image_1)

    // Load chat info when entering the screen
    LaunchedEffect(chatId) {
        chatViewModel.loadChat(chatId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                title = {
                    Text(
                        text = when {
                            chat == null -> "Loading chat..."
                            chat!!.groupChat == true -> chat!!.groupName ?: "Group Chat"
                            else -> {
                                val otherUserId =
                                    chat!!.participants.firstOrNull { it != currentUserId }
                                val otherUserName =
                                    chat!!.participantDisplayNames[otherUserId] ?: otherUserId
                                    ?: "Chat"
                                "Chat with $otherUserName"
                            }
                        }
                    )
                },
                actions = {
                    if (chat?.groupChat == true) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_people_24),
                                contentDescription = "Logout"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .drawBehind {
                    val imageWidth = imageBitmap.width.toFloat()
                    val imageHeight = imageBitmap.height.toFloat()

                    var y = 0f
                    while (y < size.height) {
                        var x = 0f
                        while (x < size.width) {
                            drawImage(
                                image = imageBitmap,
                                topLeft = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            x += imageWidth
                        }
                        y += imageHeight
                    }
                }
        )

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(messages) { message ->
                    val senderName = chat?.participantDisplayNames?.get(message.senderId) ?: "Unknown"
                    val avatarKey = chat?.participantAvatars?.get(message.senderId) ?: "avatar_default"
                    val avatarResId = AvatarUtils.getAvatarDrawableRes(avatarKey)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalAlignment = if (message.senderId == currentUserId) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (message.senderId == currentUserId) MaterialTheme.colors.primary else Color.LightGray,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Avatar
                                Image(
                                    painter = painterResource(id = avatarResId),
                                    contentDescription = "Sender Avatar",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Message content
                                Column {
                                    // Message text
                                    Text(
                                        text = message.text,
                                        color = if (message.senderId == currentUserId) Color.White else Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Name + Timestamp
                                    Row {
                                        if (message.senderId != currentUserId) {
                                            Text(
                                                text = senderName,
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = " • ",
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = formatTimestamp(message.timestamp),
                                            style = MaterialTheme.typography.caption,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF222222), shape = MaterialTheme.shapes.small), // optional: rounded dark bg
                    placeholder = { Text("Type a message...", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank()) {
                                coroutineScope.launch {
                                    chatViewModel.sendMessage(
                                        chatId = chatId,
                                        senderId = currentUserId,
                                        text = messageText
                                    )
                                    messageText = "" // Clear input
                                }
                            }
                        }
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color(0x33FFFFFF), // dark background
                        textColor = Color.White,              // white text
                        cursorColor = Color.White,            // white cursor
                        placeholderColor = Color.Gray         // gray placeholder
                    )
                )


                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        coroutineScope.launch {
                            chatViewModel.sendMessage(
                                chatId = chatId,
                                senderId = currentUserId,
                                text = messageText
                            )
                            messageText = ""
                        }
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }

    // Show Group Members Dialog if requested
    if (showDialog && chat != null) {
        GroupMembersDialog(
            memberNames = chat!!.participantDisplayNames.values.toList(),
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun GroupMembersDialog(
    memberNames: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colors.primaryVariant,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text("Group Members") },
        text = {
            Column {
                memberNames.forEach { name ->
                    Text("• $name")
                }
            }
        }
    )
}

