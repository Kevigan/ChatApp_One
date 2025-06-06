package com.example.chatapp_one

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp_one.viewModels.SessionViewModel
import com.example.chatapp_one.views.AccountView
import com.example.chatapp_one.views.ChatAppMainView
import com.example.chatapp_one.views.FriendsView
import com.example.chatapp_one.views.SettingsView
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun Navigation(
    sessionViewModel: SessionViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    googleSignInClient: GoogleSignInClient,
    googleSignInLauncher: ActivityResultLauncher<Intent>,
){
    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(Screen.MainScreen.route){
            ChatAppMainView(
                sessionViewModel = sessionViewModel,
                navController = navController,
                googleSignInClient = googleSignInClient,
                googleSignInLauncher = googleSignInLauncher
            )
        }

        composable(Screen.AccountScreen.route){
            AccountView(sessionViewModel = sessionViewModel, navController = navController)
        }

        composable(Screen.SettingsScreen.route){
            SettingsView()
        }

        composable(Screen.FriendsScreen.route){
            FriendsView()
        }
    }
}