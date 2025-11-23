package com.example.avitointership.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BookDbModel::class],
    version = 1,
    exportSchema = false,
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao() : BookDao
}