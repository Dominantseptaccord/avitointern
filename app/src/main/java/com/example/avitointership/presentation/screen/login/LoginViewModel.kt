package com.example.avitointership.presentation.screen.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.presentation.screen.login.LoginState.*

import com.example.avitointership.domain.usecase.UserUseCase.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle())
    val state = _state.asStateFlow()

    fun processCommand(command: LoginCommand) {
        when (command) {
            is LoginCommand.InputEmail -> {
                _state.update { prev ->
                    if (prev is Idle) {
                        prev.copy(email = command.email)
                    }
                    else {
                        prev
                    }
                }
            }

            is LoginCommand.InputPassword -> {
                _state.update { prev ->
                    if (prev is Idle) {
                        prev.copy(password = command.password)
                    }
                    else {
                        prev
                    }
                }
            }

            LoginCommand.Submit -> {
                val current = _state.value
                if (current !is Idle) return

                if (!Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
                    _state.value = Error("Incorrect email!")
                    return
                }
                if (current.password.isEmpty()) {
                    _state.value = Error("Password cannot be empty!")
                    return
                }

                _state.value = Loading

                viewModelScope.launch {
                    try {
                        loginUserUseCase(current.email, current.password) // suspend функция
                        _state.value = Success
                    } catch (e: Exception) {
                        _state.value = Error(e.message ?: "Login Error!")
                    }
                }
            }
        }
    }
}

sealed interface LoginCommand {
    data class InputEmail(val email: String) : LoginCommand
    data class InputPassword(val password: String) : LoginCommand
    data object Submit : LoginCommand
}

sealed interface LoginState {
    data class Idle(val email: String = "", val password: String = "") : LoginState
    data object Loading : LoginState
    data object Success : LoginState
    data class Error(val message: String) : LoginState
}
