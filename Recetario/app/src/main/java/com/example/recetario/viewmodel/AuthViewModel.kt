package com.example.recetario.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetario.data.AuthRepository
import com.example.recetario.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    val userState: StateFlow<UserPreferences> = repository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(isLoaded = false)
    )

    fun registerUser(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.registerUser(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
                gender = gender,
                email = email,
                password = password
            )
            onSuccess()
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.login(email, password))
        }
    }

    fun loginWithSavedUser(
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.loginWithSavedUser(password))
        }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
                gender = gender
            )
            onSuccess()
        }
    }

    fun setProfileImageUri(uri: String?) {
        viewModelScope.launch {
            repository.setProfileImageUri(uri)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
        }
    }

    fun setSessionActive(active: Boolean) {
        viewModelScope.launch {
            repository.setSessionActive(active)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
