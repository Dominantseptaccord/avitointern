package com.example.avitointership.domain.usecase.BookUseCase

import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.repository.BooksRepository
import jakarta.inject.Inject

class SyncAllBooksUseCase @Inject constructor(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(existingIds: List<String>): List<Book> {
        return repository.syncAllBooks(existingIds)
    }
}