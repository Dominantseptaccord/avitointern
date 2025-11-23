package com.example.avitointership.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "books"
)
data class BookDbModel(
    @PrimaryKey val id: String,
    val imageUrl: String?,
    val title: String,
    val author: String,
    val fileUrl: String,
    val userId: String = "",
    val isDownloaded: Boolean = true,
    val filePath: String? = null,
    val lastReadPosition: Long = 0,
    val fileSize: Long = 0,
)