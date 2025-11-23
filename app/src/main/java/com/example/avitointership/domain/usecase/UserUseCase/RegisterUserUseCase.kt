package com.example.avitointership.domain.usecase.UserUseCase
import com.example.avitointership.domain.repository.UserRepository
import jakarta.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repository: UserRepository
){
    suspend operator fun invoke(email: String, password: String) {
        repository.signUp(email,password)
    }
}