package com.example.avitointership.domain.usecase.BookUseCase

import android.util.Log
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.repository.BooksRepository
import jakarta.inject.Inject

class UploadBookUseCase @Inject constructor(
    private val repository: BooksRepository
)
{
    suspend operator fun invoke(
        fileUri: String,
        book: Book,
        coverImageUri: String? = null,
        onProgress: (Int) -> Unit
    ) {
        repository.uploadBook(fileUri, book, coverImageUri, onProgress)
    }
}