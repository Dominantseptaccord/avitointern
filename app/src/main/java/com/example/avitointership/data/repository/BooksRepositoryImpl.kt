package com.example.avitointership.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.example.avitointership.data.FileManager
import com.example.avitointership.data.mapper.toDbModel
import com.example.avitointership.data.mapper.toDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID

class BooksRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val fileManager: FileManager,
    @ApplicationContext private val context: Context,
) : BooksRepository {
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map {
            it.toEntities()
        }
    }

    override suspend fun syncAllBooks(existingIds: List<String>): List<Book> {
        val currentUser = auth.currentUser ?: return emptyList()
        val userId = currentUser.uid
        val snapshot = firestore.collection("books")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        val existingIdSet = existingIds.map { it.trim() }.toSet()
        val books = snapshot.documents.mapNotNull { doc ->
            val id = doc.id.trim()
            if (id in existingIdSet) return@mapNotNull null
            val title = doc.getString("title") ?: return@mapNotNull null
            val author = doc.getString("author") ?: ""
            val fileUrl = doc.getString("fileUrl") ?: ""
            val fileSize = doc.getLong("fileSize") ?: 0L
            val imgUrl = doc.getString("imgUrl")

            Book(
                id = id,
                title = title,
                author = author,
                fileUrl = fileUrl,
                userId = userId,
                fileSize = fileSize,
                imgUrl = imgUrl,
                isDownloaded = false,
            )
        }
        return books
    }

    override suspend fun addBook(book: Book) {
        val bookDbModel = book.toDbModel()
        bookDao.addBook(bookDbModel)
    }

    override suspend fun deleteBook(bookId: String) {
        val book = getBookById(bookId)
        book?.let {
            if (it.isDownloaded && it.filePath.isNotBlank()) {
                fileManager.deleteFile(it.filePath)
            }
        }
        bookDao.deleteBookById(bookId)
    }

    override suspend fun downloadBook(book: Book): String {
        return try {
            val urlWithoutParams = book.fileUrl.substringBefore('?')
            val extension = urlWithoutParams.substringAfterLast('.', "").takeIf { it.isNotBlank() } ?: "bin"
            val fileName = "${book.id}_${UUID.randomUUID()}.$extension"
            val internalFile = File(context.filesDir, fileName)
            val storageRef = storage.getReferenceFromUrl(book.fileUrl)
            storageRef.getFile(internalFile).await()
            val downloadedBook = book.copy(
                isDownloaded = true,
                filePath = internalFile.absolutePath,
                fileSize = internalFile.length()
            )
            addBook(downloadedBook)
            internalFile.absolutePath
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun uploadBook(
        fileUri: String,
        book: Book,
        coverImageUri: String?,
        onProgress: (Int) -> Unit,
    ): String {
        return try {
            var coverImageUrl: String? = null

            coverImageUri?.let { uri ->
                coverImageUrl = uploadCoverImage(uri, book.id)
            }
            val internalFilePath = fileManager.copyFileToInternalStorage(
                url = fileUri,
            )

            val localFile = File(internalFilePath)
            Log.d("Upload", "File copied to internal storage: ${localFile.absolutePath}")

            val fileName = "${book.id}.${localFile.extension}"
            val storageRef = storage.reference.child("books/$fileName")
            val uploadTask = storageRef.putFile(localFile.toUri())

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
                "imgUrl" to coverImageUrl,
                "userId" to book.userId,
                "uploadDate" to System.currentTimeMillis(),
                "fileSize" to task.totalByteCount
            )

            firestore.collection("books")
                .document(book.id)
                .set(bookData)
                .await()

            val bookWithLocalPath = book.copy(
                id = book.id,
                fileUrl = downloadUrl,
                imgUrl = coverImageUrl,
                filePath = internalFilePath,
                fileSize = localFile.length(),
                isDownloaded = true
            )
            addBook(bookWithLocalPath)

            Log.d("Upload", "Book saved with local path: ${bookWithLocalPath.filePath}")

            downloadUrl
        } catch (e: Exception) {
            throw e
        }
    }
    private suspend fun uploadCoverImage(imageUri: String, bookId: String): String {
        val internalImagePath = fileManager.copyFileToInternalStorage(imageUri)
        val localImageFile = File(internalImagePath)

        val extension = localImageFile.extension.takeIf { it.isNotBlank() } ?: "jpg"
        val imageFileName = "cover_${bookId}.$extension"

        val storageRef = storage.reference.child("covers/$imageFileName")
        val uploadTask = storageRef.putFile(localImageFile.toUri())
        uploadTask.await()

        return storageRef.downloadUrl.await().toString()
    }
    override suspend fun getBookById(bookId: String): Book? {
        return bookDao.getBookById(bookId)?.toDomain()
    }

    override suspend fun updateBookPosition(bookId: String, position: Long) {
        bookDao.updateReadingPosition(bookId, position)
    }
}