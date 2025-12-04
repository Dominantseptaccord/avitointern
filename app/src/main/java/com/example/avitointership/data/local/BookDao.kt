package com.example.avitointership.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookDbModel>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): BookDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBook(book: BookDbModel)

    @Transaction
    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)

    @Query("SELECT * FROM books WHERE userId = :userId")
    fun getBooksByUser(userId: String): Flow<List<BookDbModel>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<BookDbModel>>

    @Query("UPDATE books SET lastReadPosition = :position WHERE id = :bookId")
    suspend fun updateReadingPosition(bookId: String, position: Long)
}