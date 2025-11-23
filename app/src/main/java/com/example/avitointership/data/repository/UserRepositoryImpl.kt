package com.example.avitointership.data.repository

import android.net.Uri
import com.example.avitointership.domain.entity.User
import com.example.avitointership.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import jakarta.inject.Inject
import androidx.core.net.toUri
import com.google.firebase.auth.GoogleAuthProvider

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
): UserRepository {
    private fun requireUser() = auth.currentUser ?: throw Exception("User not logged in")
    override suspend fun signIn(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun signUp(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getCurrentUser(): User? {
        val currentUser = requireUser()
        return User(
            uid = currentUser.uid,
            displayName = currentUser.displayName ?: "",
            email = currentUser.email ?: "",
            photoUrl = currentUser.photoUrl?.toString()
        )
    }

    override suspend fun updateUserName(name: String) {
        val user = requireUser()
        val request = userProfileChangeRequest { displayName = name }
        user.updateProfile(request).await()
    }

    override suspend fun updateUserPhoto(photoUrl: String) : String {
        val user = requireUser()
        val ref = storage.reference.child("users/${user.uid}/profile.jpg")
        val imgUri = photoUrl.toUri()
        ref.putFile(imgUri).await()
        val url = ref.downloadUrl.await().toString()
        val request = userProfileChangeRequest { photoUri = Uri.parse(url) }
        user.updateProfile(request).await()
        return url
    }

    override suspend fun logout() {
        auth.signOut()
    }
}