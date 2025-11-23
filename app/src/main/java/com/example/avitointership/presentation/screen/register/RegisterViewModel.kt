package com.example.avitointership.presentation.screen.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.usecase.UserUseCase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.avitointership.presentation.screen.register.RegisterState.*

@HiltViewModel
class RegisterViewModel @Inject constructor(
    val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {
    val _state = MutableStateFlow<RegisterState>(Idle())
    val state = _state.asStateFlow()


    fun processCommand(command: RegisterCommand) {
        when(command){
            is RegisterCommand.InputEmail -> {
                _state.update { previousState ->
                    if(previousState is Idle){
                        previousState.copy(email = command.email)
                    }
                    else{
                        previousState
                    }
                }
            }
            is RegisterCommand.InputPassword -> {
                _state.update { previousState ->
                    if(previousState is Idle){
                        previousState.copy(password = command.password)
                    }
                    else{
                        previousState
                    }
                }
            }
            RegisterCommand.Submit -> {
                val current = _state.value
                if (current !is Idle) return

                if (!Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
                    _state.value = Error("Incorrect email!")
                    return
                }
                if (current.password.length < 6) {
                    _state.value = Error("Password must be at least 6 characters!")
                    return
                }

                _state.value = Loading

                viewModelScope.launch {
                    try {
                        registerUserUseCase(current.email, current.password)
                        _state.value = Success
                    } catch (e: Exception) {
                        _state.value = Error(e.message ?: "Register Error!")
                    }
                }
            }
        }
    }

}

sealed interface RegisterCommand{
    data class InputEmail(val email: String) : RegisterCommand

    data class InputPassword(val password: String) : RegisterCommand

    data object Submit : RegisterCommand
}
sealed interface RegisterState {
    data class Idle(
        val email: String = "",
        val password: String = ""
    ) : RegisterState

    data object Loading : RegisterState

    data object Success : RegisterState

    data class Error(val message: String) : RegisterState
}
