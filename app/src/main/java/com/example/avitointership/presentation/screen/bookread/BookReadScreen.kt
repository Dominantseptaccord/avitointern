package com.example.avitointership.presentation.screen.bookread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.avitointership.domain.entity.ThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReadScreen(
    modifier: Modifier = Modifier,
    viewModel: BookReadViewModel = hiltViewModel(),
    bookId: String,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settingsVisible by viewModel.settingsVisible.collectAsState()
    val progress by remember(state) {
        derivedStateOf { viewModel.calculateProgress() }
    }
    val progressPercent by remember(progress) {
        derivedStateOf { (progress * 100).toInt() }
    }
    val themeColors = when (val s = state) {
        is BookReadState.Success -> when (s.theme) {
            ReadingTheme.Light -> ThemeColors(
                background = MaterialTheme.colorScheme.surface,
                text = MaterialTheme.colorScheme.onSurface,
            )
            ReadingTheme.Dark -> ThemeColors(
                background = Color(0xFF121212),
                text = Color(0xFFFFFFFF),
            )
            ReadingTheme.Sepia -> ThemeColors(
                background = Color(0xFFF4ECD8),
                text = Color(0xFF5C4636),
            )
        }
        else -> ThemeColors(
            background = MaterialTheme.colorScheme.surface,
            text = MaterialTheme.colorScheme.onSurface,
        )
    }

    LaunchedEffect(bookId) {
        viewModel.processCommand(BookReadCommand.LoadBook(bookId))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (val s = state) {
                        is BookReadState.Success -> Text(
                            s.book.title,
                            color = themeColors.text
                        )
                        else -> Text("Reading Book", color = themeColors.text)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.Transparent)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = themeColors.text
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.processCommand(BookReadCommand.ShowSettings)
                        },
                        modifier = Modifier.background(Color.Transparent)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = themeColors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = themeColors.background,
                    navigationIconContentColor = themeColors.text,
                    titleContentColor = themeColors.text,
                    actionIconContentColor = themeColors.text
                )
            )
        },
        bottomBar = {
            when (state) {
                is BookReadState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = themeColors.text,
                            trackColor = themeColors.text.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "$progressPercent% прочитано",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = themeColors.text
                        )
                    }
                }
                else -> {}
            }
        },
        containerColor = themeColors.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val s = state) {
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
                                text = s.message,
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
                    val themeColors = when (s.theme) {
                        ReadingTheme.Light -> ThemeColors(
                            background = MaterialTheme.colorScheme.surface,
                            text = MaterialTheme.colorScheme.onSurface
                        )
                        ReadingTheme.Dark -> ThemeColors(
                            background = Color(0xFF121212),
                            text = Color(0xFFFFFFFF)
                        )
                        ReadingTheme.Sepia -> ThemeColors(
                            background = Color(0xFFF4ECD8),
                            text = Color(0xFF5C4636)
                        )
                    }
                    val scrollState = rememberScrollState()

                    LaunchedEffect(s.currentPosition) {
                        scrollState.scrollTo(s.currentPosition.toInt())
                    }

                    LaunchedEffect(scrollState.value) {
                        viewModel.saveProgress(scrollState.value)
                    }

                    Column(
                        modifier = Modifier.background(themeColors.background)
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = s.content,
                            fontSize = s.fontSize.sp,
                            lineHeight = (s.fontSize + s.lineSpacing).sp,
                            textAlign = TextAlign.Justify,
                            color = themeColors.text
                        )
                    }
                }
            }
            if (settingsVisible && state is BookReadState.Success) {
                val successState = state as BookReadState.Success

                ModalBottomSheet(
                    onDismissRequest = { viewModel.processCommand(BookReadCommand.HideSettings) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "Reading Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatSize,
                                    contentDescription = "Font Size",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Font Size",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(14, 18, 22).forEach { size ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (successState.fontSize == size)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                viewModel.processCommand(
                                                    BookReadCommand.UpdateFontSize(size)
                                                )
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (size) {
                                                14 -> "Small"
                                                18 -> "Medium"
                                                22 -> "Large"
                                                else -> ""
                                            },
                                            color = if (successState.fontSize == size)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (successState.fontSize == size)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatLineSpacing,
                                    contentDescription = "Line Spacing",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Line Spacing",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2, 4, 6).forEach { spacing ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (successState.lineSpacing == spacing)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                viewModel.processCommand(
                                                    BookReadCommand.UpdateLineSpacing(spacing)
                                                )
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (spacing) {
                                                2 -> "Compact"
                                                4 -> "Normal"
                                                6 -> "Wide"
                                                else -> ""
                                            },
                                            color = if (successState.lineSpacing == spacing)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (successState.lineSpacing == spacing)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = "Theme",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Theme",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ReadingTheme.entries.forEach { theme ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (successState.theme == theme)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    getThemeColor(theme)
                                            )
                                            .clickable {
                                                viewModel.processCommand(
                                                    BookReadCommand.UpdateTheme(theme)
                                                )
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = theme.name,
                                            color = if (successState.theme == theme)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (successState.theme == theme)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.titleMedium
                            )

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "$progressPercent% прочитано",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
@Composable
private fun getThemeColor(theme: ReadingTheme): Color {
    return when (theme) {
        ReadingTheme.Light -> MaterialTheme.colorScheme.surfaceVariant
        ReadingTheme.Dark -> Color(0xFF2D2D2D)
        ReadingTheme.Sepia -> Color(0xFFE6DCC9)
    }
}
