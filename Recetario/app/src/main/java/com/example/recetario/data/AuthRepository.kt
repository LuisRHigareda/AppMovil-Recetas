package com.example.recetario.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Locale

private val Context.authDataStore by preferencesDataStore(name = "recetario_auth_datastore")

data class UserPreferences(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val biometricEnabled: Boolean = false,
    val sessionActive: Boolean = false,
    val profileImageUri: String? = null,
    val isLoaded: Boolean = true
) {
    val hasRegisteredUser: Boolean
        get() = email.isNotBlank() && passwordHash.isNotBlank()

    val fullName: String
        get() = "$firstName $lastName".trim().ifEmpty { "Usuario" }

    val initials: String
        get() {
            val firstInitial = firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            val lastInitial = lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            return "$firstInitial$lastInitial"
                .ifBlank { "U" }
                .uppercase(Locale.getDefault())
        }
}

class AuthRepository(context: Context) {

    private val dataStore = context.applicationContext.authDataStore

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            firstName = preferences[KEY_FIRST_NAME].orEmpty(),
            lastName = preferences[KEY_LAST_NAME].orEmpty(),
            birthDate = preferences[KEY_BIRTH_DATE].orEmpty(),
            gender = preferences[KEY_GENDER].orEmpty(),
            email = preferences[KEY_EMAIL].orEmpty(),
            passwordHash = preferences[KEY_PASSWORD_HASH].orEmpty(),
            biometricEnabled = preferences[KEY_BIOMETRIC_ENABLED] ?: false,
            sessionActive = preferences[KEY_SESSION_ACTIVE] ?: false,
            profileImageUri = preferences[KEY_PROFILE_IMAGE_URI]?.ifBlank { null }
        )
    }

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        email: String,
        password: String
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_FIRST_NAME] = firstName.trim()
            preferences[KEY_LAST_NAME] = lastName.trim()
            preferences[KEY_BIRTH_DATE] = birthDate.trim()
            preferences[KEY_GENDER] = gender.trim()
            preferences[KEY_EMAIL] = email.trim().lowercase()
            preferences[KEY_PASSWORD_HASH] = hashPassword(password)
            preferences[KEY_BIOMETRIC_ENABLED] = false
            preferences[KEY_SESSION_ACTIVE] = true
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        val currentUser = userPreferencesFlow.first()
        val isValid = currentUser.email == email.trim().lowercase() &&
                currentUser.passwordHash == hashPassword(password)

        if (isValid) {
            setSessionActive(true)
        }

        return isValid
    }

    suspend fun loginWithSavedUser(password: String): Boolean {
        val currentUser = userPreferencesFlow.first()
        val isValid = currentUser.passwordHash == hashPassword(password)

        if (isValid) {
            setSessionActive(true)
        }

        return isValid
    }

    suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_FIRST_NAME] = firstName.trim()
            preferences[KEY_LAST_NAME] = lastName.trim()
            preferences[KEY_BIRTH_DATE] = birthDate.trim()
            preferences[KEY_GENDER] = gender.trim()
        }
    }

    suspend fun setProfileImageUri(uri: String?) {
        dataStore.edit { preferences ->
            preferences[KEY_PROFILE_IMAGE_URI] = uri.orEmpty()
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setSessionActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SESSION_ACTIVE] = active
        }
    }

    suspend fun logout() {
        setSessionActive(false)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val KEY_FIRST_NAME = stringPreferencesKey("first_name")
        private val KEY_LAST_NAME = stringPreferencesKey("last_name")
        private val KEY_BIRTH_DATE = stringPreferencesKey("birth_date")
        private val KEY_GENDER = stringPreferencesKey("gender")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_SESSION_ACTIVE = booleanPreferencesKey("session_active")
        private val KEY_PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    }
}
