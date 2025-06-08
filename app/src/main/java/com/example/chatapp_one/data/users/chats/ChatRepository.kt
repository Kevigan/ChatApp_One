package com.example.chatapp_one.data.users.chats

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val chatsCollection = Firebase.firestore.collection("chats")

    private fun messagesCollection(chatId: String): CollectionReference {
        return chatsCollection.document(chatId).collection("messages")
    }

    suspend fun getOrCreatePrivateChat(userA: String, userB: String): String {
        val sortedParticipants = listOf(userA, userB).sorted()

        val chatKey = sortedParticipants.joinToString("_")

        val existingChats = chatsCollection
            .whereEqualTo("chatKey", chatKey)
            .whereEqualTo("groupChat", false)
            .get()
            .await()

        if (!existingChats.isEmpty) {
            println("Chatview ChatKey exists: $chatKey")
            return existingChats.documents.first().id
        }

        // No chat → create one

        val userADisplayName = getUserDisplayName(userA)
        val userBDisplayName = getUserDisplayName(userB)

        val participantDisplayNames = mapOf(
            userA to userADisplayName,
            userB to userBDisplayName
        )

        val newChatRef = chatsCollection.document()

        val chat = Chat(
            chatId = newChatRef.id,
            participants = sortedParticipants,
            groupChat = false,
            createdBy = userA,
            createdAt = Timestamp.now(),
            chatKey = chatKey,
            participantDisplayNames = participantDisplayNames
        )

        newChatRef.set(chat).await()
        println("Chatview ChatKey does not exist: $chatKey")

        return newChatRef.id
    }

    suspend fun getChat(chatId: String): Chat {
        val doc = chatsCollection.document(chatId).get().await()
        return doc.toObject(Chat::class.java)?.copy(chatId = doc.id)
            ?: throw Exception("Chat not found")
    }

    suspend fun createGroupChat(
        participants: List<String>,
        groupName: String,
        createdBy: String
    ): String {
        val newChatRef = chatsCollection.document()

        val chat = Chat(
            chatId = newChatRef.id,
            participants = participants,
            groupChat = true,
            groupName = groupName,
            createdBy = createdBy,
            createdAt = Timestamp.now()
        )

        newChatRef.set(chat).await()

        return newChatRef.id
    }

    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val newMessageRef = messagesCollection(chatId).document()

        val message = Message(
            messageId = newMessageRef.id,
            senderId = senderId,
            text = text,
            timestamp = Timestamp.now()
        )

        newMessageRef.set(message).await()

        // Optionally update lastMessage and lastMessageTimestamp in chat doc:
        chatsCollection.document(chatId).update(
            mapOf(
                "lastMessage" to text,
                "lastMessageTimestamp" to message.timestamp
            )
        ).await()
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesCollection(chatId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Snapshot listener error: $error")
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(messageId = doc.id)
                } ?: emptyList()
                println("Snapshot listener - messages: $messages")

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getChatsForUser(userId: String): List<Chat> {
        val snapshot = chatsCollection
            .whereArrayContains("participants", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Chat::class.java)?.copy(chatId = doc.id)
        }
    }

    suspend fun getUserDisplayName(userId: String): String {
        val userDoc = Firebase.firestore.collection("users").document(userId).get().await()
        return userDoc.getString("displayName") ?: "Unknown"
    }

    suspend fun deleteChat(chatId: String) {
        chatsCollection.document(chatId).delete().await()
    }
}