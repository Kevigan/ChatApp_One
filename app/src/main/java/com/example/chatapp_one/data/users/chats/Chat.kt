package com.example.chatapp_one.data.users.chats

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val groupChat: Boolean = false,
    val groupName: String? = null,
    val participantDisplayNames: Map<String, String> = emptyMap(),
    val participantAvatars: Map<String, String> = emptyMap(),
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val chatKey: String? = null,
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val lastMessageSenderId: String? = null
)


