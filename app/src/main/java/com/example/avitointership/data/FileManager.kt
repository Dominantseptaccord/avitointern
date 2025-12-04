package com.example.avitointership.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val fileDir: File = context.filesDir

    suspend fun copyProfileImageToInternal(uri: Uri): String {
        val fileName = "profile_${System.currentTimeMillis()}.jpg" // Assuming image format is JPG
        val file = File(fileDir, fileName)

        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        }
    }
    suspend fun copyFileToInternalStorage(url: String) : String{
        val extension = getFileExtensionCustom(url.toUri())
        val fileName = "book_${UUID.randomUUID()}.$extension"
        val file = File(fileDir, fileName)

        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(url.toUri())?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        }
    }
    suspend fun deleteFile(url: String){
        withContext(Dispatchers.IO){
            val file = File(url)
            if(file.exists() && isInternal(file.absolutePath)){
                file.delete()
            }
        }
    }

    fun isInternal(url: String) : Boolean{
        return url.startsWith(fileDir.absolutePath)
    }
    fun getFileExtensionCustom(url: Uri): String? {
        return try {
            val mimeType = context.contentResolver.getType(url)
            val extensionFromMime = when (mimeType) {
                "application/pdf" -> "pdf"
                "text/plain" -> "txt"
                "application/epub+zip" -> "epub"
                else -> null
            }
            extensionFromMime
        }
        catch (e: Exception) {
            android.util.Log.e("FileManager", "Error getting extension", e)
            ""
        }
    }
}