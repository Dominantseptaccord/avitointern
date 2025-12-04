package com.example.avitointership.domain.repository

import com.example.avitointership.domain.entity.Book
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getAllBooks() : Flow<List<Book>>
    suspend fun syncAllBooks(existingIds: List<String>) : List<Book>
    suspend fun addBook(book: Book)
    suspend fun deleteBook(bookId: String)
    suspend fun downloadBook(book: Book) : String
    suspend fun uploadBook(fileUri: String, book: Book, coverImageUri: String?, onProgress: (Int) -> Unit) : String
    suspend fun getBookById(bookId: String): Book?
    suspend fun updateBookPosition(bookId: String, position: Long)
}