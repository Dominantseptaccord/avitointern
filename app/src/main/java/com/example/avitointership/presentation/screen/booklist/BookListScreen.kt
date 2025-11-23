package com.example.avitointership.presentation.screen.booklist

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.avitointership.domain.entity.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    modifier: Modifier = Modifier,
    viewModel: BookListViewModel = hiltViewModel(),
    onBookClick: (String) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Books") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Books") },
                    label = { Text("Books") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Upload") },
                    label = { Text("Upload") },
                    selected = false,
                    onClick = onNavigateToUpload
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                    Text("Retry")
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
                                    Button(onClick = onNavigateToUpload) {
                                        Text("Upload Your First Book")
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
                                viewModel.processCommand(BookListCommand.DeleteBook(book))
                            },
                            onDownloadClick = { book ->
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
        label = { Text("Search books...") },
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
                }
            )
        }
    }
}

@Composable
private fun BookListItem(
    book: Book,
    onBookClick: () -> Unit,
    onActionClick: () -> Unit
) {
    Card(
        onClick = onBookClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (book.isDownloaded) {
                    Text(
                        text = "Downloaded",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = if (book.isDownloaded) Icons.Default.Delete else Icons.Default.ArrowDropDown,
                    contentDescription = if (book.isDownloaded) "Delete" else "Download"
                )
            }
        }
    }
}