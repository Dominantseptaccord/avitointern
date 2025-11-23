package com.example.avitointership.data.repository

import android.net.Uri
import com.example.avitointership.data.local.BookDao
import com.example.avitointership.data.local.BookDbModel
import com.example.avitointership.data.mapper.toEntities
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.repository.BooksRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import com.example.avitointership.data.mapper.toDomain

class BooksRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : BooksRepository {
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map {
            it.toEntities()
        }
    }

    override suspend fun addBook(book: Book) {
        val bookDbModel = BookDbModel(
            userId = book.userId,
            id = book.id,
            imageUrl = book.imgUrl,
            title = book.title,
            author = book.author,
            fileUrl = book.fileUrl,
        )
        bookDao.addBook(bookDbModel)
    }

    override suspend fun deleteBook(bookId: String) {
        bookDao.deleteBookById(bookId)
    }

    override suspend fun downloadBook(remoteUrl: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun uploadBook(
        fileUri: String,
        book: Book,
        onProgress: (Int) -> Unit,
    ): String {
        val file = fileUri.toUri()
        val fileName = "${book.title}_${System.currentTimeMillis()}.${fileUri.substringAfterLast('.')}"
        val storageRef = storage.reference.child("books/$fileName")

        val uploadTask = storageRef.putFile(file)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            onProgress(progress)
        }

        val task = uploadTask.await()

        val downloadUrl = storageRef.downloadUrl.await().toString()

        val bookData = hashMapOf(
            "title" to book.title,
            "author" to book.author,
            "fileUrl" to downloadUrl,
            "userId" to book.userId,
            "uploadDate" to System.currentTimeMillis(),
            "fileSize" to task.totalByteCount
        )

        firestore.collection("books")
            .add(bookData)
            .await()

        val bookWithUrl = book.copy(
            id = book.id,
            fileUrl = downloadUrl,
            fileSize = task.totalByteCount
        )
        addBook(bookWithUrl)

        return downloadUrl
    }
    override suspend fun getBookById(bookId: String): Book? {
        return bookDao.getBookById(bookId)?.toDomain()
    }

    override suspend fun updateBookPosition(bookId: String, position: Long) {
        bookDao.updateReadingPosition(bookId, position)
    }
}