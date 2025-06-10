package com.example.chatapp_one.viewModels

import android.util.Log
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

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val loadedUser = userRepository.getUser(userId)
            _user.value = loadedUser

            // Load friends using existing function
            if (loadedUser != null && loadedUser.friends.isNotEmpty()) {
                loadFriendsDetails(loadedUser.friends)
            } else {
                _friends.value = emptyList()
            }
        }
    }


    fun loadFriendsDetails(friendIds: List<String>) {
        viewModelScope.launch {
            val friendUsers = userRepository.getUsersByIds(friendIds)
            _friends.value = friendUsers
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

    fun updateUserAvatar(
        userId: String,
        avatar: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                updateUser(
                    userId = userId,
                    updatedData = mapOf(
                        "avatar" to avatar
                    ),
                    onSuccess = onSuccess,
                    onError = onError
                )
            } catch (e: Exception) {
                Log.e("AvatarUpdate", "Error updating avatar: ${e.message}")
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
