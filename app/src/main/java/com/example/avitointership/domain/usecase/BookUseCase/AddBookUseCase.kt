package com.example.avitointership.domain.usecase.BookUseCase

import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.repository.BooksRepository
import kotlinx.coroutines.flow.Flow
import jakarta.inject.Inject

class AddBookUseCase @Inject constructor(
    private val repository: BooksRepository
)
{
    suspend operator fun invoke(book: Book) {
        repository.addBook(book)
    }
}