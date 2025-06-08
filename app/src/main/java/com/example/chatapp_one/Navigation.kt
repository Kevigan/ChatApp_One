package com.example.chatapp_one

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatapp_one.viewModels.ChatViewModel
import com.example.chatapp_one.viewModels.SessionViewModel
import com.example.chatapp_one.viewModels.UserViewModel
import com.example.chatapp_one.views.AccountView
import com.example.chatapp_one.views.ChatAppMainView
import com.example.chatapp_one.views.ChatView
import com.example.chatapp_one.views.FriendsView
import com.example.chatapp_one.views.SettingsView
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun Navigation(
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel,
    chatViewModel: ChatViewModel,
    navController: NavHostController = rememberNavController(),
    googleSignInClient: GoogleSignInClient,
    googleSignInLauncher: ActivityResultLauncher<Intent>,
){
    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(Screen.MainScreen.route){
            ChatAppMainView(
                userViewModel = userViewModel,
                sessionViewModel = sessionViewModel,
                chatViewModel = chatViewModel,
                navController = navController,
                googleSignInClient = googleSignInClient,
                googleSignInLauncher = googleSignInLauncher
            )
        }

        composable(Screen.AccountScreen.route){
            AccountView(userViewModel = userViewModel ,sessionViewModel = sessionViewModel, navController = navController)
        }

        composable(Screen.SettingsScreen.route){
            SettingsView()
        }

        composable(Screen.FriendsScreen.route){
            FriendsView(userViewModel, chatViewModel, navController)
        }

        composable(
            route = Screen.ChatScreen.routeWithPlaceholder,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable

            ChatView(
                chatId = chatId,
                currentUserId = sessionViewModel.currentUser.value?.uid ?: "", // pass currentUserId!
                chatViewModel = chatViewModel
            )
        }
    }
}