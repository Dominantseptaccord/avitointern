package com.example.avitointership.presentation.screen.booklist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.avitointership.domain.entity.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    modifier: Modifier = Modifier,
    viewModel: BookListViewModel = hiltViewModel(),
    onBookClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.processCommand(BookListCommand.LoadBooks)
    }
    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("Мои книги") },
            actions = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Обновить книги",
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable {
                            viewModel.processCommand(BookListCommand.LoadBooks)
                        }
                )
            }
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(
                    query = state.query,
                    onQueryChange = { viewModel.processCommand(BookListCommand.InputQuery(it)) },
                    modifier = Modifier.padding(16.dp)
                )

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = state.error!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(onClick = { viewModel.processCommand(BookListCommand.LoadBooks) }) {
                                    Text("Заново")
                                }
                            }
                        }
                    }

                    state.emptyMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    state.emptyMessage!!,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (state.emptyMessage == "You do not have any books yet.") {
                                    Button(onClick = {  }) {
                                        Text("Загрузите свою первую книгу")
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        BookList(
                            books = state.books,
                            onBookClick = onBookClick,
                            onDeleteClick = { book ->
                                Toast.makeText(context, "Удаление книги...", Toast.LENGTH_SHORT).show()
                                viewModel.processCommand(BookListCommand.DeleteBook(book))
                            },
                            onDownloadClick = { book ->
                                Toast.makeText(context, "Загрузка книги...", Toast.LENGTH_SHORT).show()
                                viewModel.processCommand(BookListCommand.DownloadBook(book))
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Поиск книг…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun BookList(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    onDeleteClick: (Book) -> Unit,
    onDownloadClick: (Book) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(books) { book ->
            BookListItem(
                book = book,
                onBookClick = { onBookClick(book.id) },
                onActionClick = {
                    if (book.isDownloaded) {
                        onDeleteClick(book)
                    } else {
                        onDownloadClick(book)
                    }
                },
                isClickable = book.isDownloaded
            )
        }
    }
}

@Composable
private fun BookListItem(
    book: Book,
    onBookClick: () -> Unit,
    onActionClick: () -> Unit,
    isClickable: Boolean
) {
    Card(
        onClick = if (isClickable) {
            { onBookClick() }
        } else {
            { }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = isClickable
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp)
            ) {
                if (book.imgUrl != null && book.imgUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.imgUrl,
                        contentDescription = "Обложка книги",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Нет обложки",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!isClickable) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (book.isDownloaded) {
                    Text(
                        text = "Скачана",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Доступна для скачивания",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = if (book.isDownloaded) Icons.Default.Delete else Icons.Default.CloudDownload,
                    contentDescription = if (book.isDownloaded) "Удалить" else "Скачать",
                    tint = if (book.isDownloaded) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}