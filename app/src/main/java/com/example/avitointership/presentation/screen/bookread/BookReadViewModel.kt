package com.example.avitointership.presentation.screen.bookread

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.usecase.BookUseCase.GetBookByIdUseCase
import com.example.avitointership.domain.usecase.BookUseCase.UpdateBookPositionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookReadViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookPositionUseCase: UpdateBookPositionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BookReadState>(BookReadState.Loading)
    val state = _state.asStateFlow()

    fun processCommand(command: BookReadCommand) {
        when (command) {
            is BookReadCommand.LoadBook -> loadBook(command.bookId)
            is BookReadCommand.UpdatePosition -> updatePosition(command.bookId, command.position)
            is BookReadCommand.UpdateFontSize -> updateFontSize(command.size)
            is BookReadCommand.UpdateLineSpacing -> updateLineSpacing(command.spacing)
            is BookReadCommand.UpdateTheme -> updateTheme(command.theme)
        }
    }

    private fun loadBook(bookId: String) {
        viewModelScope.launch {
            _state.value = BookReadState.Loading
            try {
                val book = getBookByIdUseCase(bookId)
                if (book != null) {
                    // TODO: Load actual book content from file
                    val content = "This is sample book content for ${book.title} by ${book.author}..."

                    _state.value = BookReadState.Success(
                        book = book,
                        content = content,
                        fontSize = 16,
                        lineSpacing = 0,
                        theme = ReadingTheme.Light,
                        currentPosition = book.lastReadPosition
                    )
                } else {
                    _state.value = BookReadState.Error("Book not found")
                }
            } catch (e: Exception) {
                _state.value = BookReadState.Error("Failed to load book: ${e.message}")
            }
        }
    }

    private fun updatePosition(bookId: String, position: Long) {
        viewModelScope.launch {
            updateBookPositionUseCase(bookId, position)
            _state.value = (_state.value as? BookReadState.Success)?.copy(
                currentPosition = position
            ) ?: _state.value
        }
    }

    private fun updateFontSize(size: Int) {
        _state.value = (_state.value as? BookReadState.Success)?.copy(
            fontSize = size
        ) ?: _state.value
    }

    private fun updateLineSpacing(spacing: Int) {
        _state.value = (_state.value as? BookReadState.Success)?.copy(
            lineSpacing = spacing
        ) ?: _state.value
    }

    private fun updateTheme(theme: ReadingTheme) {
        _state.value = (_state.value as? BookReadState.Success)?.copy(
            theme = theme
        ) ?: _state.value
    }
}

sealed class BookReadCommand {
    data class LoadBook(val bookId: String) : BookReadCommand()
    data class UpdatePosition(val bookId: String, val position: Long) : BookReadCommand()
    data class UpdateFontSize(val size: Int) : BookReadCommand()
    data class UpdateLineSpacing(val spacing: Int) : BookReadCommand()
    data class UpdateTheme(val theme: ReadingTheme) : BookReadCommand()
}

sealed class BookReadState {
    object Loading : BookReadState()
    data class Success(
        val book: Book,
        val content: String,
        val fontSize: Int,
        val lineSpacing: Int,
        val theme: ReadingTheme,
        val currentPosition: Long
    ) : BookReadState()
    data class Error(val message: String) : BookReadState()
}

enum class ReadingTheme {
    Light, Dark, Sepia
}