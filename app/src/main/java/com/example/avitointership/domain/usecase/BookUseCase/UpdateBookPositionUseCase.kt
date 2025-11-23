package com.example.avitointership.domain.usecase.BookUseCase

import com.example.avitointership.domain.repository.BooksRepository
import jakarta.inject.Inject

class UpdateBookPositionUseCase @Inject constructor(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(bookId: String, position: Long) {
        repository.updateBookPosition(bookId, position)
    }
}