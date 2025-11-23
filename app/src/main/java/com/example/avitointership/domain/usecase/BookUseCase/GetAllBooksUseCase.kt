package com.example.avitointership.domain.usecase.BookUseCase

import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.repository.BooksRepository
import kotlinx.coroutines.flow.Flow
import jakarta.inject.Inject

class GetAllBooksUseCase @Inject constructor(
    private val repository: BooksRepository
)
    {
    operator fun invoke() : Flow<List<Book>>{
        return repository.getAllBooks()
    }
}