package com.example.avitointership.presentation.screen.bookread

import android.text.Html
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.usecase.BookUseCase.GetBookByIdUseCase
import com.example.avitointership.domain.usecase.BookUseCase.UpdateBookPositionUseCase
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookReadViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookPositionUseCase: UpdateBookPositionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BookReadState>(BookReadState.Loading)
    private val _settingsVisible = MutableStateFlow(false)
    private val _showDeleteConfirm = MutableStateFlow(false)
    val state = _state.asStateFlow()
    val showDeleteConfirm = _showDeleteConfirm.asStateFlow()
    val settingsVisible = _settingsVisible.asStateFlow()

    fun processCommand(command: BookReadCommand) {
        when (command) {
            is BookReadCommand.LoadBook -> {
                loadBook(command.bookId)
            }
            is BookReadCommand.UpdatePosition -> updatePosition(command.bookId, command.position)
            is BookReadCommand.UpdateFontSize -> updateFontSize(command.size)
            is BookReadCommand.UpdateLineSpacing -> updateLineSpacing(command.spacing)
            is BookReadCommand.UpdateTheme -> updateTheme(command.theme)
            BookReadCommand.ShowSettings -> showSettings()
            BookReadCommand.HideSettings -> hideSettings()
        }
    }

    private fun loadBook(bookId: String) {
        viewModelScope.launch {
            _state.value = BookReadState.Loading
            try {
                val book = getBookByIdUseCase(bookId)
                if (book == null) {
                    _state.value = BookReadState.Error("Book not found $bookId")
                    return@launch
                }
                if (!book.isDownloaded) {
                    _state.value = BookReadState.Error("Book is not downloaded yet. Please wait or restart app.")
                    return@launch
                }
                if (book.filePath.isBlank()) {
                    _state.value = BookReadState.Error("Book file path is empty")
                    return@launch
                }
                val content = loadBookContent(book)

                _state.value = BookReadState.Success(
                    book = book,
                    content = content,
                    fontSize = 16,
                    lineSpacing = 4,
                    theme = ReadingTheme.Light,
                    currentPosition = book.lastReadPosition
                )
            } catch (e: Exception) {
                _state.value = BookReadState.Error("Failed to load book: ${e.message}")
            }
        }
    }
    private fun loadBookContent(book: Book): String {
        return try {

            val file = File(book.filePath)
            when {
                book.filePath.endsWith(".txt") -> file.readText()
                book.filePath.endsWith(".pdf") -> extractTextFromPdf(file)
                book.filePath.endsWith(".epub") -> extractTextFromEpub(file)
                else -> {
                    book.filePath
                }
            }
        } catch (e: Exception) {
            "Error reading book: ${e.message}"
        }
    }
    fun extractTextFromPdf(file: File): String {
        return try {
            val reader = PdfReader(file.absolutePath)
            val n = reader.numberOfPages
            val text = StringBuilder()
            for (i in 1..n) {
                text.append(PdfTextExtractor.getTextFromPage(reader, i).trim())
                text.append(" ")
            }
            reader.close()
            text.toString()
        } catch (e: Exception) {
            "Error reading PDF: ${e.message}"
        }
    }

    fun extractTextFromEpub(file: File): String {
        return try {
            val reader = EpubReader()
            val book = reader.readEpub(file.inputStream())

            val sb = StringBuilder()
            for (resource in book.contents) {
                val html = resource.data.toString(Charsets.UTF_8)
                val plain = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
                sb.append(plain).append("\n\n")
            }
            sb.toString()
        } catch (e: Exception) {
            "Error reading EPUB: ${e.message}"
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
    private fun showSettings() {
        _settingsVisible.value = true
    }

    private fun hideSettings() {
        _settingsVisible.value = false
    }

    private fun showDeleteConfirm() {
        _showDeleteConfirm.value = true
    }

    private fun hideDeleteConfirm() {
        _showDeleteConfirm.value = false
    }

    fun calculateProgress(): Float {
        return when (val current = _state.value) {
            is BookReadState.Success -> {
                if (current.content.isNotEmpty()) {
                    (current.currentPosition.toFloat() / current.content.length.toFloat())
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            else -> 0f
        }
    }
    fun saveProgress(position: Int) {
        viewModelScope.launch {
            delay(300)
            when (val current = _state.value) {
                is BookReadState.Success -> {
                    if (position.toLong() > current.currentPosition) {
                        updateBookPositionUseCase(current.book.id, position.toLong())
                        _state.value = current.copy(currentPosition = position.toLong())
                    }
                }
                else -> {}
            }
        }
    }
}

sealed class BookReadCommand {
    data class LoadBook(val bookId: String) : BookReadCommand()
    data class UpdatePosition(val bookId: String, val position: Long) : BookReadCommand()
    data class UpdateFontSize(val size: Int) : BookReadCommand()
    data class UpdateLineSpacing(val spacing: Int) : BookReadCommand()
    data class UpdateTheme(val theme: ReadingTheme) : BookReadCommand()
    object ShowSettings : BookReadCommand()
    object HideSettings : BookReadCommand()
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