package com.example.chatapp_one.data.users.chats

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)

