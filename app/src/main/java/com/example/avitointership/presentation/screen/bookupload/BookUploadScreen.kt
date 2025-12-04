package com.example.avitointership.presentation.screen.bookupload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookUploadScreen(
    modifier: Modifier = Modifier,
    onUploadSuccess: () -> Unit = {},
    viewModel: BookUploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state is BookUploadState.Success) {
            viewModel.processCommand(BookUploadCommand.Reset)
        }
    }

    LaunchedEffect(state) {
        if (state is BookUploadState.Success) {
            onUploadSuccess()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val fileName = getFileNameFromUri(context, uri)
                viewModel.processCommand(
                    BookUploadCommand.FileSelected(
                        fileUri = uri.toString(),
                        fileName = fileName
                    )
                )
            } catch (e: Exception) {
                Log.e("BookUpload", "Error getting file info", e)
                val fallbackName = uri.lastPathSegment?.substringAfterLast("/") ?: "book.pdf"
                viewModel.processCommand(
                    BookUploadCommand.FileSelected(
                        fileUri = uri.toString(),
                        fileName = fallbackName
                    )
                )
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processCommand(
                BookUploadCommand.CoverImageSelected(
                    coverUri = uri.toString()
                )
            )
        }
    }

    when (val s = state) {
        is BookUploadState.Uploading -> {
            UploadInProgressScreen(
                progress = s.progress,
                onCancel = { }
            )
        }

        is BookUploadState.Error -> {
            ErrorScreen(
                errorMessage = s.message,
                onRetry = { viewModel.processCommand(BookUploadCommand.Retry) },
                onDismiss = { viewModel.processCommand(BookUploadCommand.Reset) }
            )
        }

        is BookUploadState.Success -> {
            SuccessScreen(
                onContinue = {
                    viewModel.processCommand(BookUploadCommand.Reset)
                    onUploadSuccess()
                }
            )
        }

        is BookUploadState.Idle -> {
            Column(modifier = modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = { Text("Загрузить книгу") }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (s.selectedCoverUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = Uri.parse(s.selectedCoverUri)
                                ),
                                contentDescription = "Обложка книги",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Нет обложки",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.selectedFileName ?: "Выберите файл")
                    }

                    OutlinedTextField(
                        value = s.title,
                        onValueChange = { viewModel.processCommand(BookUploadCommand.InputTitle(it)) },
                        label = { Text("Название книги *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = s.author,
                        onValueChange = { viewModel.processCommand(BookUploadCommand.InputAuthor(it)) },
                        label = { Text("Автор *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val isFormValid = s.title.isNotBlank() &&
                            s.author.isNotBlank() &&
                            s.selectedFileUri != null

                    Button(
                        onClick = { viewModel.processCommand(BookUploadCommand.Upload) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid
                    ) {
                        Text("Загрузить книгу")
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadInProgressScreen(
    progress: Int,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Загрузка книги", style = MaterialTheme.typography.headlineMedium)
            CircularProgressIndicator()
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Пожалуйста, подождите...",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onCancel) {
                Text("Отменить загрузку")
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Закрыть")
                }
                Button(onClick = onRetry) {
                    Text("Попробовать снова")
                }
            }
        }
    }
}

@Composable
private fun SuccessScreen(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Книга загружена!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Ваша книга успешно загружена.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onContinue) {
                Text("Продолжить")
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    return when (uri.scheme) {
        "content" -> {
            val docFile = DocumentFile.fromSingleUri(context, uri)
            docFile?.name?.takeIf { it.isNotBlank() } ?: run {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex >= 0) {
                            cursor.getString(displayNameIndex) ?: "unknown"
                        } else {
                            "unknown"
                        }
                    } else {
                        "unknown"
                    }
                } ?: "unknown"
            }
        }
        "file" -> {
            File(uri.path ?: "").name
        }
        else -> {
            uri.lastPathSegment?.substringAfterLast("/") ?: "unknown"
        }
    }.takeIf { it.isNotBlank() } ?: "book.pdf"
}