package com.example.avitointership.domain.usecase.UserUseCase

import com.example.avitointership.domain.repository.UserRepository
import jakarta.inject.Inject

class GoogleAuthUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(idToken: String) {
        userRepository.signInWithGoogle(idToken)
    }
}