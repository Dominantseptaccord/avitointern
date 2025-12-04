package com.example.avitointership.presentation.screen.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.entity.Book
import com.example.avitointership.domain.usecase.BookUseCase.AddBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.DeleteBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.DownloadBookUseCase
import com.example.avitointership.domain.usecase.BookUseCase.GetAllBooksUseCase
import com.example.avitointership.domain.usecase.BookUseCase.SyncAllBooksUseCase
import com.example.avitointership.presentation.screen.booklist.BookListState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BookListViewModel @Inject constructor(
    val syncAllBooksUseCase: SyncAllBooksUseCase,
    val deleteBookUseCase: DeleteBookUseCase,
    val downloadBookUseCase: DownloadBookUseCase,
    val getAllBooksUseCase: GetAllBooksUseCase,
) : ViewModel() {
    val _state = MutableStateFlow(BookListState())
    val state = _state.asStateFlow()
    private var allBooks = listOf<Book>()
    init {
        loadBooks()
        observeLocalBooks()
    }
    fun processCommand(command: BookListCommand){
        when(command){
            is BookListCommand.DeleteBook -> {
                viewModelScope.launch {
                    try {
                        deleteBookUseCase(command.book.id)
                        allBooks = allBooks.mapNotNull { currentBook ->
                            if (currentBook.id == command.book.id) {
                                if (currentBook.fileUrl.isNotEmpty()) {
                                    currentBook.copy(isDownloaded = false, filePath = "")
                                } else {
                                    null
                                }
                            } else {
                                currentBook
                            }
                        }

                        filterBooks(_state.value.query)

                    } catch (e: Exception) {
                        _state.update { it.copy(
                            error = "Ошибка удаления: ${e.message}"
                        ) }
                    }
                }
            }
            is BookListCommand.DownloadBook -> {
                viewModelScope.launch {
                    try {
                        downloadBookUseCase(command.book)
                    } catch (e: Exception) {
                        _state.update { it.copy(
                            error = "Ошибка загрузки: ${e.message}"
                        ) }
                    }
                }
            }
            BookListCommand.LoadBooks -> {
                loadBooks()
            }
            is BookListCommand.InputQuery -> {
                filterBooks(command.query)
            }
        }
    }
    private fun observeLocalBooks() {
        getAllBooksUseCase()
            .onEach { localBooks ->
                val localBooksMap = localBooks.associateBy { it.id }
                allBooks = allBooks.map { book ->
                    localBooksMap[book.id]?.let { localBook ->
                        localBook.copy(isDownloaded = true)
                    } ?: book
                } + localBooks.filter { localBook ->
                    allBooks.none { it.id == localBook.id }
                }
                filterBooks(_state.value.query)
            }
            .launchIn(viewModelScope)
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val localBooks = getAllBooksUseCase().first()
                val remoteBooks = syncAllBooksUseCase(localBooks.map { it.id })
                val localBooksMap = localBooks.associateBy { it.id }
                val mergedBooks = mutableListOf<Book>()

                mergedBooks.addAll(localBooks)
                remoteBooks.forEach { remoteBook ->
                    if (localBooksMap[remoteBook.id] == null) {
                        mergedBooks.add(remoteBook.copy(isDownloaded = false))
                    }
                }

                allBooks = mergedBooks
                filterBooks(_state.value.query)

            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Ошибка загрузки: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    private fun filterBooks(query: String) {
        val filteredBooks = if (query.isBlank()) {
            allBooks
        } else {
            val lowerQuery = query.lowercase()
            allBooks.filter { book ->
                (book.isDownloaded) && (
                        book.title.lowercase().contains(lowerQuery) ||
                                book.author.lowercase().contains(lowerQuery)
                        )
            }
        }
        _state.update {
            it.copy(
                query = query,
                books = filteredBooks,
                emptyMessage = getEmptyMessage(filteredBooks, query),
                isLoading = false
            )
        }
    }
    private fun getEmptyMessage(books: List<Book>, query: String): String? {
        return when {
            books.isEmpty() && query.isNotBlank() -> "Ничего не найдено по запросу: $query"
            books.isEmpty() -> "У вас пока нет книг"
            else -> null
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
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val emptyMessage: String? = null
)