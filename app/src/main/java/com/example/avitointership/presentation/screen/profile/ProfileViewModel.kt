package com.example.avitointership.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avitointership.domain.entity.User
import com.example.avitointership.domain.usecase.UserUseCase.GetCurrentUserUseCase
import com.example.avitointership.domain.usecase.UserUseCase.LogoutUseCase
import com.example.avitointership.domain.usecase.UserUseCase.UpdateUserPhotoUseCase
import com.example.avitointership.domain.usecase.UserUseCase.UpdateUserNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
    private val updateUserPhotoUseCase: UpdateUserPhotoUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state = _state.asStateFlow()

    init {
        loadUser()
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.LoadUser -> loadUser()
            is ProfileCommand.UpdateName -> updateName(command.name)
            is ProfileCommand.UpdatePhoto -> updatePhoto(command.photoUri)
            ProfileCommand.Logout -> logout()
            ProfileCommand.ClearError -> clearError()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                val user = getCurrentUserUseCase()
                if (user != null) {
                    _state.value = ProfileState.Success(user)
                } else {
                    _state.value = ProfileState.Error("User not found")
                }
            } catch (e: Exception) {
                _state.value = ProfileState.Error("Failed to load user: ${e.message}")
            }
        }
    }

    private fun updateName(name: String) {
        viewModelScope.launch {
            try {
                updateUserNameUseCase(name)
                loadUser()
            } catch (e: Exception) {
                _state.value = ProfileState.Error("Failed to update name: ${e.message}")
            }
        }
    }

    private fun updatePhoto(photoUri: String) {
        viewModelScope.launch {
            try {
                _state.value = ProfileState.Loading
                updateUserPhotoUseCase(photoUri)
                delay(500)
                loadUser()
            } catch (e: Exception) {
                _state.value = ProfileState.Error("Failed to update photo: ${e.message}")
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                _state.value = ProfileState.LogoutSuccess
            } catch (e: Exception) {
                _state.value = ProfileState.Error("Failed to logout: ${e.message}")
            }
        }
    }

    private fun clearError() {
        loadUser()
    }
}

sealed class ProfileCommand {
    object LoadUser : ProfileCommand()
    data class UpdateName(val name: String) : ProfileCommand()
    data class UpdatePhoto(val photoUri: String) : ProfileCommand()
    object Logout : ProfileCommand()
    object ClearError : ProfileCommand()
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
    object LogoutSuccess : ProfileState()
}