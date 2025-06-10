package com.example.chatapp_one.data.users

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val friends: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val avatar: String = "avatar_1"
)

