package com.example.chatapp_one.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp_one.data.users.User
import com.example.chatapp_one.data.users.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _userFriends = MutableStateFlow<List<User>>(emptyList())
    val userFriends: StateFlow<List<User>> = _userFriends.asStateFlow()


    fun loadUser(userId: String) {
        viewModelScope.launch {
            val loadedUser = userRepository.getUser(userId)
            _user.value = loadedUser

            // Now load friends
            if (loadedUser != null && loadedUser.friends.isNotEmpty()) {
                val loadedFriends = mutableListOf<User>()
                for (friendId in loadedUser.friends) {
                    val friendUser = userRepository.getUser(friendId)
                    if (friendUser != null) {
                        loadedFriends.add(friendUser)
                    }
                }
                _userFriends.value = loadedFriends
            } else {
                _userFriends.value = emptyList()
            }
        }
    }

    fun checkOrCreateUser(
        userId: String,
        displayName: String,
        email: String,
        onUserExists: () -> Unit,
        onUserCreated: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val created = userRepository.checkOrCreateUser(userId, displayName, email)
                if (created) {
                    onUserCreated()
                } else {
                    onUserExists()
                }
                // Load user into state
                loadUser(userId)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateUser(userId: String, updatedData: Map<String, Any>, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(userId, updatedData)
                onSuccess()
                // Optionally reload user:
                loadUser(userId)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun getUserIdByEmail(
        email: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val friendUserId = userRepository.getUserIdByEmail(email)
                onSuccess(friendUserId)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun addFriendByEmail(
        currentUserId: String,
        friendEmail: String,
        onSuccess: () -> Unit,
        onUserNotFound: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val friendUserId = userRepository.getUserIdByEmail(friendEmail)
                if (friendUserId != null) {
                    userRepository.addFriend(currentUserId, friendUserId)
                    onSuccess()
                    loadUser(currentUserId) // reload state
                } else {
                    onUserNotFound()
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(userId)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

}
