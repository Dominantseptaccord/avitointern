package com.example.avitointership.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.avitointership.R
import com.example.avitointership.presentation.screen.booklist.BookListScreen
import com.example.avitointership.presentation.screen.bookread.BookReadScreen
import com.example.avitointership.presentation.screen.bookupload.BookUploadScreen
import com.example.avitointership.presentation.screen.login.LoginScreen
import com.example.avitointership.presentation.screen.profile.ProfileScreen
import com.example.avitointership.presentation.screen.register.RegisterScreen

@Composable
fun MainApp() {
    val navController = rememberNavController()

    MainScreen(
        navController = navController,
        onLogout = {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    )
}

@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = when (currentRoute) {
        Screen.BookList.route,
        Screen.BookUpload.route,
        Screen.Profile.route -> true
        else -> false
    }

    val selectedItem = when (currentRoute) {
        Screen.BookList.route -> 0
        Screen.BookUpload.route -> 1
        Screen.Profile.route -> 2
        else -> 0
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Book, contentDescription = null) },
                        label = { Text(stringResource(R.string.books)) },
                        selected = selectedItem == 0,
                        onClick = {
                            if (currentRoute != Screen.BookList.route) {
                                navController.navigate(Screen.BookList.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Screen.BookList.route) { inclusive = false }
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text(stringResource(R.string.upload)) },
                        selected = selectedItem == 1,
                        onClick = {
                            if (currentRoute != Screen.BookUpload.route) {
                                navController.navigate(Screen.BookUpload.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Screen.BookUpload.route) { inclusive = false }
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(stringResource(R.string.profile)) },
                        selected = selectedItem == 2,
                        onClick = {
                            if (currentRoute != Screen.Profile.route) {
                                navController.navigate(Screen.Profile.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Screen.Profile.route) { inclusive = false }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            paddingValues = paddingValues,
            onLogout = onLogout
        )
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = androidx.compose.ui.Modifier.padding(paddingValues)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BookList.route) {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookReader.createRoute(bookId))
                }
            )
        }

        composable(Screen.BookUpload.route) {
            BookUploadScreen(
                onUploadSuccess = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.BookUpload.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = onLogout
            )
        }

        composable(Screen.BookReader.route) {
            val bookId = Screen.BookReader.getBookId(it.arguments)
            BookReadScreen(
                bookId = bookId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object BookList : Screen("book_list")
    object BookUpload : Screen("book_upload")
    object Profile : Screen("profile")

    object BookReader : Screen("book_reader/{book_id}") {
        fun createRoute(bookId: String): String {
            return "book_reader/$bookId"
        }

        fun getBookId(arguments: android.os.Bundle?): String {
            return arguments?.getString("book_id") ?: ""
        }
    }
}