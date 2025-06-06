package com.example.chatapp_one

import androidx.annotation.DrawableRes

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("main_screen", "Main")
    object AddFriendScreen : Screen("addfriend_screen", "AddFriend")
    object AccountScreen : Screen("account_screen", "Account")
    object SettingsScreen : Screen("settings_screen", "Settings")
    object FriendsScreen : Screen("friends_screen", "Friends")
}