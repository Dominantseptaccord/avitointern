package com.example.avitointership.data.repository

import android.net.Uri
import android.util.Log
import com.example.avitointership.domain.entity.User
import com.example.avitointership.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import jakarta.inject.Inject
import androidx.core.net.toUri
import com.example.avitointership.data.FileManager
import com.google.firebase.auth.GoogleAuthProvider
import androidx.core.net.toUri
import java.io.File

class UserRepositoryImpl @Inject constructor(
    private val fileManager: FileManager,
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

    override suspend fun updateUserPhoto(photoUri: String) : String {
        val user = requireUser()
        val contentUri = photoUri.toUri()
        val internalPath = fileManager.copyProfileImageToInternal(contentUri)
        val internalFileUri = Uri.fromFile(File(internalPath))
        val fileName = "profile_${user.uid}_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("users/${user.uid}/$fileName")
        try {
            ref.putFile(internalFileUri).await()
            val  remoteUrl = ref.downloadUrl.await().toString()

            val request = userProfileChangeRequest {
                this.photoUri = Uri.parse(remoteUrl)
            }
            user.updateProfile(request).await()
            return internalFileUri.toString()
        } catch (e: Exception) {
            Log.e("PhotoUpdate", "Firebase upload failed!", e)
            throw e
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}