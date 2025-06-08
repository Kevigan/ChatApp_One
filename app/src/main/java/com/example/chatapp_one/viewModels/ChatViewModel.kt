package com.example.chatapp_one.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp_one.data.users.chats.Chat
import com.example.chatapp_one.data.users.chats.ChatRepository
import com.example.chatapp_one.data.users.chats.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    // Chats for current user
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat.asStateFlow()

    // Load chats for current user
    fun loadChatsForUser(userId: String) {
        viewModelScope.launch {
            val chatList = chatRepository.getChatsForUser(userId)
                .sortedByDescending { it.lastMessageTimestamp }
            _chats.value = chatList
        }
    }

    fun loadChat(chatId: String) {
        viewModelScope.launch {
            val chat = chatRepository.getChat(chatId)
            _chat.value = chat
        }
    }

    // Create private chat and return chatId
    suspend fun getOrCreatePrivateChat(userA: String, userB: String): String {
        return chatRepository.getOrCreatePrivateChat(userA, userB)
    }

    // Create group chat and return chatId
    suspend fun createGroupChat(participants: List<String>, groupName: String, createdBy: String): String {
        return chatRepository.createGroupChat(participants, groupName, createdBy)
    }

    // Send message
    fun sendMessage(chatId: String, senderId: String, text: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(chatId, senderId, text)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Observe messages (Flow) — used in ChatView
    fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessagesFlow(chatId)
    }

    // Delete chat (optional, if you want this feature)
    fun deleteChat(chatId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        viewModelScope.launch {
            try {
                chatRepository.deleteChat(chatId)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}