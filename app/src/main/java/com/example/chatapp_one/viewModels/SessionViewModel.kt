package com.example.chatapp_one.viewModels

import androidx.lifecycle.ViewModel
import com.example.chatapp_one.data.users.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ViewModel for managing Firebase authentication state and actions.
// Handles login, registration, password updates, account deletion, and sign-out.
// Exposes current user as StateFlow and integrates with Google Sign-In client.

class SessionViewModel : ViewModel() {

    // Holds the current Firebase user; updates when auth state changes.
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Listens for authentication state changes and updates the current user.
    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
        }
    }

    fun signOut(googleSignInClient: GoogleSignInClient? = null) {
        auth.signOut()
        googleSignInClient?.signOut()
    }

    fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun reauthenticate(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteUser(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return
        user.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (userId: String, displayName: String, email: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = auth.currentUser
                val userId = firebaseUser?.uid ?: return@addOnSuccessListener
                val displayName = firebaseUser.displayName ?: email.substringBefore("@")
                val userEmail = firebaseUser.email ?: email

                onSuccess(userId, displayName, userEmail)
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                val displayName = email.substringBefore("@")

                //create the user in Firestore:
                createUserInFirestore(
                    userId = userId,
                    displayName = displayName,
                    email = email,
                    onSuccess = { onSuccess() },
                    onFailure = { onFailure(it) }
                )
            }
            .addOnFailureListener { onFailure(it) }
    }

    // when registered via email
    fun createUserInFirestore(
        userId: String,
        displayName: String,
        email: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val user = User(
            userId = userId,
            displayName = displayName,
            email = email,
            friends = emptyList(),
            createdAt = Timestamp.now()
        )

        Firebase.firestore
            .collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun handleGoogleSignInResult(
        resultData: android.content.Intent?,
        onSuccess: (userId: String, displayName: String, email: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(resultData)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)

            com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val userId = firebaseUser?.uid ?: return@addOnSuccessListener
                    val displayName = firebaseUser.displayName ?: ""
                    val email = firebaseUser.email ?: ""

                    onSuccess(userId, displayName, email)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            onError(e)
        }
    }
}
