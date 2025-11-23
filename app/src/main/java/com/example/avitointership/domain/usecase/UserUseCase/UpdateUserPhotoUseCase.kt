package com.example.avitointership.domain.usecase.UserUseCase

import com.example.avitointership.domain.repository.UserRepository
import jakarta.inject.Inject

class UpdateUserPhotoUseCase @Inject constructor(
    private val repository: UserRepository
){
    suspend operator fun invoke(photoUrl: String) : String {
        return repository.updateUserPhoto(photoUrl)
    }
}