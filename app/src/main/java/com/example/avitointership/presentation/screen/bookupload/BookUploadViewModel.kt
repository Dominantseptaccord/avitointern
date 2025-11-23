package com.example.avitointership.presentation.screen.bookupload

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
import java.util.UUID

import javax.inject.Inject

@HiltViewModel
class BookUploadViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val addBookUseCase: AddBookUseCase,
    private val uploadBookUseCase: UploadBookUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BookUploadState>(Idle)
    val state = _state.asStateFlow()

    private var selectedFileUri: String? = null
    private var currentTitle = ""
    private var currentAuthor = ""

    fun processCommand(command: BookUploadCommand) {
        when (command) {
            is BookUploadCommand.FileSelected -> {
                _state.value = FileSelected(command.fileName)
            }
            is BookUploadCommand.InputTitle -> {
                currentTitle = command.title
            }
            is BookUploadCommand.InputAuthor -> {
                currentAuthor = command.author
            }
            BookUploadCommand.Upload -> {
                if (validateForm()) {
                    uploadBook()
                }
            }
            BookUploadCommand.Retry -> {
                if (validateForm()) {
                    uploadBook()
                }
            }
            BookUploadCommand.ClearError -> {
                _state.value = BookUploadState.Idle
            }
        }
    }

    private fun validateForm(): Boolean {
        if (selectedFileUri == null) {
            _state.value = Error("Select the book file")
            return false
        }
        if (currentTitle.isBlank()) {
            _state.value = Error("Enter the title of the book")
            return false
        }
        if (currentAuthor.isBlank()) {
            _state.value = Error("Enter the book's author")
            return false
        }

        val fileExtension = selectedFileUri?.substringAfterLast('.', "")
        if (fileExtension !in listOf("txt", "epub", "pdf")) {
            _state.value = Error("Only files are supported .txt, .epub, .pdf")
            return false
        }

        return true
    }

    private fun uploadBook() {
        viewModelScope.launch {
            _state.value = BookUploadState.Uploading(0)
            try {
                val book = Book(
                    id = UUID.randomUUID().toString(),
                    userId = getCurrentUserId(),
                    title = currentTitle,
                    author = currentAuthor,
                    imgUrl = null,
                    fileUrl = "",
                    isDownloaded = false,
                    filePath = "",
                    fileSize = 0,
                    progress = 0,
                    lastReadPosition = 0
                )

                uploadBookUseCase(
                    fileUri = selectedFileUri!!,
                    book = book,
                    onProgress = { progress ->
                        _state.value = BookUploadState.Uploading(progress)
                    }
                )

                _state.value = Success
            } catch (e: Exception) {
                _state.value = Error(message = "Error Upload")
            }
        }
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser!!.uid;
    }
}

sealed class BookUploadCommand {
    data class FileSelected(val fileUri: String, val fileName: String) : BookUploadCommand()
    data class InputTitle(val title: String) : BookUploadCommand()
    data class InputAuthor(val author: String) : BookUploadCommand()
    object Upload : BookUploadCommand()
    object Retry : BookUploadCommand()
    object ClearError : BookUploadCommand()
}

sealed class BookUploadState {
    object Idle : BookUploadState()
    data class FileSelected(val fileName: String) : BookUploadState()
    data class Uploading(val progress: Int) : BookUploadState()
    object Success : BookUploadState()
    data class Error(val message: String) : BookUploadState()
}