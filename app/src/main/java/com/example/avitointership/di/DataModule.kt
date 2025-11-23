package com.example.avitointership.di

import android.content.Context
import androidx.room.Room
import com.example.avitointership.data.local.BookDao
import com.example.avitointership.data.local.BookDatabase
import com.example.avitointership.data.repository.BooksRepositoryImpl
import com.example.avitointership.data.repository.UserRepositoryImpl
import com.example.avitointership.domain.repository.BooksRepository
import com.example.avitointership.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindBookRepository(
        impl: BooksRepositoryImpl
    ) : BooksRepository

    @Binds
    @Singleton
    fun bindUserRepository(
        impl: UserRepositoryImpl
    ) : UserRepository

    companion object {
        @Provides
        @Singleton
        fun provideBookDatabase(
            @ApplicationContext context: Context
        ) : BookDatabase{
            return Room.databaseBuilder(
                context = context,
                klass = BookDatabase::class.java,
                name = "book.db"
            ).fallbackToDestructiveMigration(true).build()
        }

        @Provides
        @Singleton
        fun provideBookDao(
            database: BookDatabase
        ) : BookDao{
            return database.bookDao()
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth() : FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseStorage() : FirebaseStorage = FirebaseStorage.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseFirestore() : FirebaseFirestore = FirebaseFirestore.getInstance()

    }
}