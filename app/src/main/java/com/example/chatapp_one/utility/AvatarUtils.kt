package com.example.chatapp_one.utility

import com.example.chatapp_one.R

object AvatarUtils {

    fun getAvatarDrawableRes(avatar: String): Int {
        return when (avatar) {
            "avatar_1" -> R.drawable.test_avatar_1
            "avatar_2" -> R.drawable.test_avatar_2
            "avatar_3" -> R.drawable.test_avatar_3
            "avatar_4" -> R.drawable.test_avatar_4
            else -> R.drawable.avatar_default
        }
    }
}