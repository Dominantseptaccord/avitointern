package com.example.avitointership.domain.repository

import com.example.avitointership.domain.entity.User

interface UserRepository {
    suspend fun signIn(email: String, password: String)

    suspend fun signUp(email: String, password: String)

    suspend fun signInWithGoogle(idToken: String)
    fun getCurrentUser() : User?
    suspend fun updateUserName(name: String)
    suspend fun updateUserPhoto(photoUrl: String) : String
    suspend fun logout()


}