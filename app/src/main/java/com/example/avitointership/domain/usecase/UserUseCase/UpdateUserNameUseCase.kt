package com.example.avitointership.domain.usecase.UserUseCase

import com.example.avitointership.domain.entity.User
import com.example.avitointership.domain.repository.UserRepository
import jakarta.inject.Inject

class UpdateUserNameUseCase @Inject constructor(
    private val repository: UserRepository
){
    suspend operator fun invoke(name: String)  {
        return repository.updateUserName(name)
    }
}