package com.example.avitointership.domain.usecase.BookUseCase

import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.repository.BooksRepository
import jakarta.inject.Inject

class GetBookByIdUseCase @Inject constructor(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Book? {
        return repository.getBookById(bookId)
    }
}