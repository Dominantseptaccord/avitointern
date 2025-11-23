package com.example.avitointership.domain.usecase.UserUseCase

import com.example.avitointership.domain.entity.User
import com.example.avitointership.domain.repository.UserRepository
import jakarta.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
){
    operator fun invoke() : User? {
        return repository.getCurrentUser()
    }
}