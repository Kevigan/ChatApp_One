package com.example.chatapp_one.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatapp_one.viewModels.UserViewModel

@Composable
fun FriendsView(userViewModel: UserViewModel) {

    val userFriends by userViewModel.userFriends.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            if (userFriends.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("No friends to show.")
                    }
                }
            } else {
                items(userFriends) { friendUser ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = friendUser.displayName)
                        Divider()
                    }
                }
            }
        }
    }
}
