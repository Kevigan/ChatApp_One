package com.example.chatapp_one.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.chatapp_one.utility.formatTimestamp
import com.example.chatapp_one.viewModels.ChatViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    // Load chat info when entering the screen
    LaunchedEffect(chatId) {
        chatViewModel.loadChat(chatId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            chat == null -> "Loading chat..."
                            chat!!.groupChat == true -> chat!!.groupName ?: "Group Chat"
                            else -> {
                                val otherUserId = chat!!.participants.firstOrNull { it != currentUserId }
                                val otherUserName = chat!!.participantDisplayNames[otherUserId] ?: otherUserId ?: "Chat"
                                "Chat with $otherUserName"
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
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
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = message.text,
                                    color = if (message.senderId == currentUserId) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatTimestamp(message.timestamp),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.caption
                                )
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
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
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
}
