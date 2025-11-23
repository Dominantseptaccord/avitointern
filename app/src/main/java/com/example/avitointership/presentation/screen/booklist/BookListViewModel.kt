package com.example.avitointership.presentation.screen.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.usecase.BookUseCase.AddBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.DeleteBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.DownloadBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.GetAllBooksUseCase
import com.example.avitointership.presentation.screen.booklist.BookListState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BookListViewModel @Inject constructor(
    val addBookUseCase: AddBookUseCase,
    val deleteBookUseCase: DeleteBookUseCase,
    val downloadBookUseCase: DownloadBookUseCase,
    val getAllBooksUseCase: GetAllBooksUseCase,
) : ViewModel() {
    val _state = MutableStateFlow<BookListState>(BookListState())
    val state = _state.asStateFlow()
    init {
        observeBooks()
    }
    fun processCommand(command: BookListCommand){
        when(command){
            is BookListCommand.DeleteBook -> {
                viewModelScope.launch {
                    deleteBookUseCase(command.book)
                }
            }
            is BookListCommand.DownloadBook -> {
                viewModelScope.launch {
                    downloadBookUseCase(command.book)
                }
            }
            BookListCommand.LoadBooks -> {
                viewModelScope.launch {
                    TODO()
                }
            }
            is BookListCommand.InputQuery -> {
                _state.update { it.copy(query = command.query) }
                filterBooks()
            }
        }
    }
    private fun observeBooks() {
        getAllBooksUseCase().onEach { list ->
            val q = state.value.query.lowercase()
            val filtered = if (q.isBlank()) {
                list
            } else {
                list.filter {
                    it.title.lowercase().contains(q) ||
                            it.author.lowercase().contains(q)
                }
            }
            _state.update {
                it.copy(
                    books = filtered,
                    emptyMessage = when {
                        list.isEmpty() -> "You do not have any books yet."
                        filtered.isEmpty() -> "Nothing found"
                        else -> null
                    }
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun filterBooks() {
        val current = state.value
        val q = current.query.lowercase()

        val filtered = if (q.isBlank()) {
            current.books
        } else {
            current.books.filter {
                it.title.lowercase().contains(q) ||
                        it.author.lowercase().contains(q)
            }
        }

        _state.update {
            it.copy(
                books = filtered,
                emptyMessage = when {
                    filtered.isEmpty() && q.isNotBlank() -> "Nothing found"
                    else -> null
                }
            )
        }
    }
}

sealed interface BookListCommand {
    data class InputQuery(val query: String) : BookListCommand
    data object LoadBooks : BookListCommand
    data class DeleteBook(val book: Book) : BookListCommand
    data class DownloadBook(val book: Book) : BookListCommand
}

data class BookListState(
    val query: String = "",
    val books: List<Book> = listOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val emptyMessage: String? = null
)