package com.example.avitointership.presentation.screen.register

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.usecase.UserUseCase.GoogleAuthUseCase
import com.example.avitointership.domain.usecase.UserUseCase.RegisterUserUseCase
import com.example.avitointership.presentation.screen.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.avitointership.presentation.screen.register.RegisterState.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class RegisterViewModel @Inject constructor(
    val registerUserUseCase: RegisterUserUseCase,
    val googleRegisterUseCase: GoogleAuthUseCase,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val _state = MutableStateFlow<RegisterState>(Idle())
    val state = _state.asStateFlow()

    init {
        firebaseAuth.currentUser?.let {
            _state.value = Success
        }
    }

    fun getGoogleSignInClient(webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

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

            is RegisterCommand.GoogleSignIn -> {
                viewModelScope.launch {
                    _state.value = Loading
                    try {
                        googleRegisterUseCase(command.idToken)
                        _state.value = Success
                    } catch (e: Exception) {
                        _state.value = Error(e.message ?: "Google Sign In Error!")
                    }
                }
            }
        }
    }
  }

sealed interface RegisterCommand{
    data class InputEmail(val email: String) : RegisterCommand

    data class InputPassword(val password: String) : RegisterCommand
    data class GoogleSignIn(val idToken: String) : RegisterCommand

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
