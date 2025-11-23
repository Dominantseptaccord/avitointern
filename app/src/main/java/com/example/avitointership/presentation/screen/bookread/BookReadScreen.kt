package com.example.avitointership.presentation.screen.bookread

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReadScreen(
    modifier: Modifier = Modifier,
    viewModel: BookReadViewModel = hiltViewModel(),
    bookId: String,
    onBack: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(bookId) {
        viewModel.processCommand(BookReadCommand.LoadBook(bookId))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (state) {
                        is BookReadState.Success -> Text(state.book.title)
                        else -> Text("Reading Book")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            when (state) {
                is BookReadState.Success -> {
                    LinearProgressIndicator(
                        progress = (state.currentPosition.toFloat() / state.content.length.toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is BookReadState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BookReadState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.processCommand(BookReadCommand.LoadBook(bookId)) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is BookReadState.Success -> {
                    val scrollState = rememberScrollState()

                    LaunchedEffect(scrollState.value) {
                        viewModel.processCommand(
                            BookReadCommand.UpdatePosition(
                                bookId = bookId,
                                position = scrollState.value.toLong()
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = state.content,
                            fontSize = state.fontSize.sp,
                            lineHeight = (state.fontSize + state.lineSpacing).sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        }
    }
}