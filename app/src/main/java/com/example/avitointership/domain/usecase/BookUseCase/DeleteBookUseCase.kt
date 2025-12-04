package com.example.avitointership.domain.usecase.BookUseCase

import com.example.avitointership.domain.repository.BooksRepository
import jakarta.inject.Inject

class DeleteBookUseCase @Inject constructor(
    private val repository: BooksRepository
)
{
    suspend operator fun invoke(id: String) {
        repository.deleteBook(id)
    }
}