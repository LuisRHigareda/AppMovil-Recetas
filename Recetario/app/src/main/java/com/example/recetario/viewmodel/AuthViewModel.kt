package com.example.recetario.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetario.data.AuthRepository
import com.example.recetario.data.LoginResult
import com.example.recetario.data.RecipeRepository
import com.example.recetario.data.RegisterUserResult
import com.example.recetario.data.UserPreferences
import com.example.recetario.worker.SyncManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val recipeRepository = RecipeRepository(application)

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
        onResult: (RegisterUserResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerUser(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
                gender = gender,
                email = email,
                password = password
            )
            onResult(result)
            SyncManager.scheduleProfileSync(getApplication())
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (LoginResult) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Intentamos el login local
            var result = repository.login(email, password)

            // OFFLINE-FIRST: Si el login FALLÓ localmente (!result.isValid),
            // asumimos que es porque borró los datos o es celular nuevo.
            if (!result.isValid) {
                // 2. ¡Rescate! Intentamos traerlo de Firebase
                val synced = repository.syncProfileFromCloud(email)

                if (synced) {
                    // 3. Si se descargó con éxito, volvemos a intentar el login local
                    // ahora que los datos y el passwordHash ya viven en el DataStore
                    result = repository.login(email, password)
                }
            }

            // 4. Si después de todo el proceso el login es exitoso
            if(result.isValid){
                // Lo lanzamos en una nueva corrutina para no bloquear la UI.
                // El usuario entrará al Home y verá cómo las recetas aparecen mágicamente.
                viewModelScope.launch {
                    recipeRepository.restoreUserDataFromCloud(email)
                }
            }

            onResult(result)
        }
    }

    fun loginWithSavedUser(
        password: String,
        onResult: (LoginResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginWithSavedUser(password)

            // OFFLINE-FIRST: Igual aquí, si entra con biometría o sesión guardada
            if (result.isValid) {
                // Como en esta función no pasamos el email por parámetro,
                // lo sacamos del userState actual.
                val currentUserEmail = userState.value.email
                if (currentUserEmail.isNotEmpty()) {
                    viewModelScope.launch {
                        recipeRepository.restoreUserDataFromCloud(currentUserEmail)
                    }
                }
            }

            onResult(result)
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

            SyncManager.scheduleProfileSync(getApplication())

            onSuccess()
        }
    }

    fun setProfileImageUri(uri: String?) {
        viewModelScope.launch {
            repository.setProfileImageUri(uri)
            SyncManager.scheduleProfileSync(getApplication())
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
