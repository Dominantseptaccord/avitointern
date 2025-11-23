package com.example.avitointership.domain.entity

data class User(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?
)
