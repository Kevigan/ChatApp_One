package com.example.chatapp_one.utility

import java.text.SimpleDateFormat
import java.util.Locale

fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val date = timestamp.toDate()
    return sdf.format(date)
}

