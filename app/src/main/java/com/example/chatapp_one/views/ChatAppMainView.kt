package com.example.chatapp_one.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Divider
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatapp_one.R

@Composable
fun ChatAppMainView() {
    Scaffold(
        topBar = { ChatAppTopBar() },
        bottomBar = { ChatAppBottomBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Start new chat */ }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        ChatListContent(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ChatAppTopBar() {
    TopAppBar(
        title = { Text("ChatApp") },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White
    )
}

@Composable
fun ChatAppBottomBar() {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding().height(70.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomBarButton("Add Friend", R.drawable.baseline_person_add_24) { }
            BottomBarButton("Create Group", R.drawable.baseline_group_add_24) { }
            BottomBarButton("Friends", R.drawable.baseline_people_24) { }
            BottomBarButton("Settings", R.drawable.baseline_settings_24) { }
        }
    }
}

@Composable
fun BottomBarButton(label: String, @DrawableRes iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(text = label, fontSize = 10.sp)
    }
}



@Composable
fun ChatListContent(modifier: Modifier = Modifier) {
    val sampleChats = remember {
        listOf("Alice", "Bob", "Charlie", "Dev Group", "Study Buddies")
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        LazyColumn {
            items(sampleChats, key = { it }) { chatName ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Text(
                        text = chatName,
                        color =  MaterialTheme.colors.primary,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Last message preview...",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
                Divider()
            }
        }
    }

}




