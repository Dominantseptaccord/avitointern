package com.example.avitointership.domain.entity


data class Book(
    val userId: String,
    val id: String,
    val title: String,
    val author: String,
    val imgUrl: String?,
    val fileUrl: String = "",
    val isDownloaded: Boolean = false,
    val filePath: String = "",
    val fileSize: Long = 0,
    val progress: Int = 0,
    val lastReadPosition: Long = 0
)
