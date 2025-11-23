package com.example.avitointership.data.mapper

import com.example.avitointership.data.local.BookDbModel
import com.example.avitointership.domain.entity.Book

fun List<BookDbModel>.toEntities() : List<Book> {
    return map {
        Book(
            userId = it.userId,
            id = it.id,
            title = it.title,
            author = it.author,
            imgUrl = it.imageUrl,
            fileUrl = it.fileUrl,
            isDownloaded = it.isDownloaded,
            filePath = it.filePath ?: "",
            fileSize = it.fileSize,
            progress = if (it.isDownloaded) 100 else 0,
            lastReadPosition = it.lastReadPosition
        )
    }
}
fun Book.toDbModel() : BookDbModel{
    return BookDbModel(
        id = id,
        imageUrl = imgUrl,
        title = title,
        author = author,
        fileUrl = fileUrl,
        userId = userId,
        isDownloaded = isDownloaded,
        filePath = filePath.ifEmpty { null },
        lastReadPosition = lastReadPosition,
        fileSize = fileSize
    )
}

fun BookDbModel.toDomain(): Book {
    return Book(
        id = this.id,
        title = this.title,
        author = this.author,
        imgUrl = this.imageUrl,
        fileUrl = this.fileUrl,
        isDownloaded = this.isDownloaded,
        filePath = this.filePath ?: "",
        progress = if (this.isDownloaded) 100 else 0,
        lastReadPosition = this.lastReadPosition,
        userId = this.userId,
        fileSize = this.fileSize
    )
}