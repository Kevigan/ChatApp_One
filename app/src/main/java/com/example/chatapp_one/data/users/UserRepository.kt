package com.example.chatapp_one.data.users

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val usersCollection = Firebase.firestore.collection("users")

    // Check if user exists → if not, create it → returns true if created, false if already existed
    suspend fun checkOrCreateUser(userId: String, displayName: String, email: String): Boolean {
        val userDoc = usersCollection.document(userId).get().await()
        return if (userDoc.exists()) {
            false // user already exists
        } else {
            val newUser = User(
                userId = userId,
                displayName = displayName,
                email = email,
                friends = emptyList(),
                createdAt = Timestamp.now()
            )
            usersCollection.document(userId).set(newUser).await()
            true // user was created
        }
    }

    suspend fun getUser(userId: String): User? {
        val userDoc = usersCollection.document(userId).get().await()
        return userDoc.toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, updatedData: Map<String, Any>) {
        usersCollection.document(userId).update(updatedData).await()
    }

    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        val snapshot = Firebase.firestore.collection("users")
            .whereIn("userId", userIds)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }


    suspend fun getUserIdByEmail(email: String): String? {
        val result = usersCollection
            .whereEqualTo("email", email)
            .get()
            .await()

        return result.documents.firstOrNull()?.id
    }

    suspend fun addFriend(userId: String, friendUserId: String) {
        usersCollection.document(userId)
            .update("friends", com.google.firebase.firestore.FieldValue.arrayUnion(friendUserId))
            .await()
    }

    suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
    }

}