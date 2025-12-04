package com.example.avitointership.presentation.screen.bookupload

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.usecase.BookUseCase.AddBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.UploadBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.avitointership.presentation.screen.bookupload.BookUploadState.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BookUploadViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val addBookUseCase: AddBookUseCase,
    private val uploadBookUseCase: UploadBookUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BookUploadState>(Idle())
    val state = _state.asStateFlow()

    fun processCommand(command: BookUploadCommand) {
        when (command) {
            is BookUploadCommand.FileSelected -> {
                viewModelScope.launch {
                    _state.update {previousState ->
                        if(previousState is Idle) {
                            previousState.copy(
                                selectedFileUri = command.fileUri,
                                selectedFileName = command.fileName,
                            )
                        }
                        else{
                            previousState
                        }
                    }
                }
            }
            is BookUploadCommand.InputTitle -> {
                viewModelScope.launch {

                    _state.update {previousState ->
                        if(previousState is Idle) {
                            previousState.copy(title = command.title)
                        }
                        else{
                            previousState
                        }
                    }

                }
            }
            is BookUploadCommand.InputAuthor -> {
                viewModelScope.launch {
                    _state.update {previousState ->
                        if(previousState is Idle) {
                            previousState.copy(author = command.author)
                        }
                        else{
                            previousState
                        }
                    }
                }
            }
            BookUploadCommand.Upload -> {
                viewModelScope.launch {
                    if (validateForm()) {
                        uploadBook()
                    }
                }
            }
            BookUploadCommand.Retry -> {
                viewModelScope.launch {
                    if (validateForm()) {
                        uploadBook()
                    }
                }
            }
            BookUploadCommand.Reset -> {
                viewModelScope.launch {
                    _state.value = Idle()
                }
            }

            is BookUploadCommand.CoverImageSelected -> {
                viewModelScope.launch {
                    _state.update { previousState ->
                        if (previousState is Idle) {
                            previousState.copy(
                                selectedCoverUri = command.coverUri,
                            )
                        } else {
                            previousState
                        }
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        _state.update { previousState ->
            if(previousState is Idle){
                if (previousState.selectedFileUri == null){
                    Error("Select the book file")
                    return false
                }
                if (previousState.title.isBlank()) {
                    Error("Enter the title of the book")
                    return false
                }
                if (previousState.author.isBlank()) {
                    Error("Enter the book's author")
                    return false
                }
                return true
            }
            else{
                return true
            }
        }



        return true
    }

    private fun uploadBook() {
        viewModelScope.launch {

            val current = _state.value
            if (current !is Idle) return@launch

            try {
                _state.value = Uploading(progress = 0)

                val book = Book(
                    id = UUID.randomUUID().toString(),
                    userId = getCurrentUserId(),
                    title = current.title,
                    author = current.author,
                    imgUrl = null,
                    fileUrl = "",
                    isDownloaded = false,
                    filePath = "",
                    fileSize = 0,
                    progress = 0,
                    lastReadPosition = 0
                )

                uploadBookUseCase(
                    fileUri = current.selectedFileUri!!,
                    book = book,
                    coverImageUri = current.selectedCoverUri,
                    onProgress = { progress ->
                        _state.value = Uploading(progress)
                    }
                )


                _state.value = Success

            } catch (e: Exception) {
                _state.value = Error("Loading error: ${e.message}")
            }
        }
    }


    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("The user is not authorized")
    }
}

sealed class BookUploadCommand {
    data class FileSelected(val fileUri: String, val fileName: String) : BookUploadCommand()
    data class CoverImageSelected(val coverUri: String) : BookUploadCommand()
    data class InputTitle(val title: String) : BookUploadCommand()
    data class InputAuthor(val author: String) : BookUploadCommand()
    object Upload : BookUploadCommand()
    object Retry : BookUploadCommand()
    object Reset : BookUploadCommand()
}

sealed class BookUploadState {
    data class Idle(
        val title: String = "",
        val author: String = "",
        val selectedFileName: String? = null,
        val selectedFileUri: String? = null,
        val selectedCoverUri: String? = null
    ) : BookUploadState()
    data class Uploading(val progress: Int) : BookUploadState()
    object Success : BookUploadState()
    data class Error(val message: String) : BookUploadState()
}
