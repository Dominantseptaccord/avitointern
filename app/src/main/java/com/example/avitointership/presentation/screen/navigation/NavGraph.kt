package com.example.avitointership.presentation.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.avitointership.presentation.screen.booklist.BookListScreen
import com.example.avitointership.presentation.screen.bookread.BookReadScreen
import com.example.avitointership.presentation.screen.bookupload.BookUploadScreen
import com.example.avitointership.presentation.screen.login.LoginScreen
import com.example.avitointership.presentation.screen.profile.ProfileScreen
import com.example.avitointership.presentation.screen.register.RegisterScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Profile.route) {
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
                },
                onNavigateToUpload = {
                    navController.navigate(Screen.BookUpload.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.BookUpload.route) {
            BookUploadScreen(
                onUploadSuccess = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.BookList.route) { inclusive = true }
                    }
                },
                onNavigateToBooks = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.BookList.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.BookUpload.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onNavigateToBooks = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                onNavigateToUpload = {
                    navController.navigate(Screen.BookUpload.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
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

        fun getBookId(arguments: Bundle?): String {
            return arguments?.getString("book_id") ?: ""
        }
    }
}