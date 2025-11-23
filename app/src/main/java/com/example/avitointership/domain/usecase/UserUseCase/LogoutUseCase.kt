package com.example.avitointership.domain.usecase.UserUseCase

import com.example.avitointership.domain.repository.UserRepository
import jakarta.inject.Inject
class LogoutUseCase @Inject constructor(
    private val repository: UserRepository
){
    suspend operator fun invoke() {
        return repository.logout()
    }
}