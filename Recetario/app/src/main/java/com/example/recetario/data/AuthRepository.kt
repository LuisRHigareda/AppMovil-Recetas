package com.example.recetario.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    val biometricSetupCompleted: Boolean = false,
    val sessionActive: Boolean = false,
    val profileImageUri: String? = null,
    val registeredEmails: Set<String> = emptySet(),
    val isLoaded: Boolean = true,
    val isProfileSynced: Boolean = false
) {
    val hasRegisteredUser: Boolean
        get() = email.isNotBlank() && passwordHash.isNotBlank()

    val hasAnyRegisteredUser: Boolean
        get() = registeredEmails.isNotEmpty()

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

enum class RegisterUserResult {
    Success,
    EmailAlreadyExists
}

data class LoginResult(
    val isValid: Boolean,
    val requiresBiometricSetup: Boolean = false
)

class AuthRepository(context: Context) {

    private val dataStore = context.applicationContext.authDataStore

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val registeredEmails = preferences[KEY_REGISTERED_EMAILS].orEmpty()
        val currentEmail = preferences[KEY_CURRENT_EMAIL].orEmpty().normalizeEmail()

        if (currentEmail.isBlank()) {
            UserPreferences(
                registeredEmails = registeredEmails,
                sessionActive = preferences[KEY_SESSION_ACTIVE] ?: false
            )
        } else {
            UserPreferences(
                firstName = preferences[firstNameKey(currentEmail)].orEmpty(),
                lastName = preferences[lastNameKey(currentEmail)].orEmpty(),
                birthDate = preferences[birthDateKey(currentEmail)].orEmpty(),
                gender = preferences[genderKey(currentEmail)].orEmpty(),
                email = currentEmail,
                passwordHash = preferences[passwordHashKey(currentEmail)].orEmpty(),
                biometricEnabled = preferences[biometricEnabledKey(currentEmail)] ?: false,
                biometricSetupCompleted = preferences[biometricSetupCompletedKey(currentEmail)]
                    ?: (preferences[biometricEnabledKey(currentEmail)] ?: false),
                sessionActive = preferences[KEY_SESSION_ACTIVE] ?: false,
                profileImageUri = preferences[profileImageUriKey(currentEmail)]?.ifBlank { null },
                registeredEmails = registeredEmails,
                isProfileSynced = preferences[isProfileSyncedKey(currentEmail)] ?: false
            )
        }
    }

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        email: String,
        password: String
    ): RegisterUserResult {
        val normalizedEmail = email.normalizeEmail()
        val currentPreferences = dataStore.data.first()
        val registeredEmails = currentPreferences[KEY_REGISTERED_EMAILS].orEmpty()
        val alreadyExists = normalizedEmail in registeredEmails ||
                currentPreferences[passwordHashKey(normalizedEmail)].orEmpty().isNotBlank()

        if (alreadyExists) {
            return RegisterUserResult.EmailAlreadyExists
        }

        dataStore.edit { preferences ->
            val updatedEmails = preferences[KEY_REGISTERED_EMAILS].orEmpty().toMutableSet()
            updatedEmails.add(normalizedEmail)

            preferences[KEY_REGISTERED_EMAILS] = updatedEmails
            preferences[KEY_CURRENT_EMAIL] = normalizedEmail
            preferences[firstNameKey(normalizedEmail)] = firstName.trim()
            preferences[lastNameKey(normalizedEmail)] = lastName.trim()
            preferences[birthDateKey(normalizedEmail)] = birthDate.trim()
            preferences[genderKey(normalizedEmail)] = gender.trim()
            preferences[passwordHashKey(normalizedEmail)] = hashPassword(password)
            preferences[biometricEnabledKey(normalizedEmail)] = false
            preferences[biometricSetupCompletedKey(normalizedEmail)] = false
            preferences[profileImageUriKey(normalizedEmail)] = ""
            preferences[KEY_SESSION_ACTIVE] = true
            preferences[isProfileSyncedKey(normalizedEmail)] = false
        }

        return RegisterUserResult.Success
    }

    suspend fun login(email: String, password: String): LoginResult {
        val normalizedEmail = email.normalizeEmail()
        val preferences = dataStore.data.first()
        val savedPasswordHash = preferences[passwordHashKey(normalizedEmail)].orEmpty()
        val isValid = savedPasswordHash.isNotBlank() && savedPasswordHash == hashPassword(password)

        if (!isValid) {
            return LoginResult(isValid = false)
        }

        val biometricEnabled = preferences[biometricEnabledKey(normalizedEmail)] ?: false
        val biometricSetupCompleted = preferences[biometricSetupCompletedKey(normalizedEmail)] ?: biometricEnabled

        dataStore.edit { editablePreferences ->
            editablePreferences[KEY_CURRENT_EMAIL] = normalizedEmail
            editablePreferences[KEY_SESSION_ACTIVE] = true
        }

        return LoginResult(
            isValid = true,
            requiresBiometricSetup = !biometricSetupCompleted
        )
    }

    suspend fun loginWithSavedUser(password: String): LoginResult {
        val currentUser = userPreferencesFlow.first()
        val isValid = currentUser.email.isNotBlank() &&
                currentUser.passwordHash == hashPassword(password)

        if (!isValid) {
            return LoginResult(isValid = false)
        }

        setSessionActive(true)

        return LoginResult(
            isValid = true,
            requiresBiometricSetup = !currentUser.biometricSetupCompleted
        )
    }

    suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String
    ) {
        val currentEmail = userPreferencesFlow.first().email

        if (currentEmail.isBlank()) return

        dataStore.edit { preferences ->
            preferences[firstNameKey(currentEmail)] = firstName.trim()
            preferences[lastNameKey(currentEmail)] = lastName.trim()
            preferences[birthDateKey(currentEmail)] = birthDate.trim()
            preferences[genderKey(currentEmail)] = gender.trim()
            preferences[isProfileSyncedKey(currentEmail)] = false
        }
    }

    suspend fun setProfileImageUri(uri: String?) {
        val currentEmail = userPreferencesFlow.first().email

        if (currentEmail.isBlank()) return

        dataStore.edit { preferences ->
            preferences[profileImageUriKey(currentEmail)] = uri.orEmpty()
            preferences[isProfileSyncedKey(currentEmail)] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        val currentEmail = userPreferencesFlow.first().email

        if (currentEmail.isBlank()) return

        dataStore.edit { preferences ->
            preferences[biometricEnabledKey(currentEmail)] = enabled
            preferences[biometricSetupCompletedKey(currentEmail)] = true
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

    suspend fun markProfileAsSynced(email: String) {
        val normalizedEmail = email.normalizeEmail()
        dataStore.edit { preferences ->
            preferences[isProfileSyncedKey(normalizedEmail)] = true
        }
    }

    // OFFLINE-FIRST: Descarga el perfil de la nube y lo guarda localmente
    suspend fun syncProfileFromCloud(email: String): Boolean {
        val firebaseDataSource = com.example.recetario.data.firebase.FirebaseDataSource()
        val remoteProfile = firebaseDataSource.getUserProfile(email.normalizeEmail())

        return if (remoteProfile != null) {
            dataStore.edit { preferences ->
                val normalizedEmail = email.normalizeEmail()

                // Registramos el email en la lista de cuentas del dispositivo
                val updatedEmails = preferences[KEY_REGISTERED_EMAILS].orEmpty().toMutableSet()
                updatedEmails.add(normalizedEmail)
                preferences[KEY_REGISTERED_EMAILS] = updatedEmails

                // Guardamos todos los datos descargados
                preferences[firstNameKey(normalizedEmail)] = remoteProfile.firstName
                preferences[lastNameKey(normalizedEmail)] = remoteProfile.lastName
                preferences[birthDateKey(normalizedEmail)] = remoteProfile.birthDate
                preferences[genderKey(normalizedEmail)] = remoteProfile.gender
                preferences[passwordHashKey(normalizedEmail)] = remoteProfile.passwordHash
                preferences[profileImageUriKey(normalizedEmail)] = remoteProfile.profileImageUri.orEmpty()

                // Como viene de la nube, marcamos que ya está sincronizado
                preferences[isProfileSyncedKey(normalizedEmail)] = true
            }
            true
        } else {
            false
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val KEY_REGISTERED_EMAILS = stringSetPreferencesKey("registered_emails")
        private val KEY_CURRENT_EMAIL = stringPreferencesKey("current_email")
        private val KEY_SESSION_ACTIVE = booleanPreferencesKey("session_active")

        private fun String.normalizeEmail(): String = trim().lowercase(Locale.getDefault())

        private fun userKey(email: String): String {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest(email.normalizeEmail().toByteArray())

            return bytes.joinToString("") { "%02x".format(it) }.take(32)
        }

        private fun firstNameKey(email: String) = stringPreferencesKey("user_${userKey(email)}_first_name")
        private fun lastNameKey(email: String) = stringPreferencesKey("user_${userKey(email)}_last_name")
        private fun birthDateKey(email: String) = stringPreferencesKey("user_${userKey(email)}_birth_date")
        private fun genderKey(email: String) = stringPreferencesKey("user_${userKey(email)}_gender")
        private fun passwordHashKey(email: String) = stringPreferencesKey("user_${userKey(email)}_password_hash")
        private fun biometricEnabledKey(email: String) = booleanPreferencesKey("user_${userKey(email)}_biometric_enabled")
        private fun biometricSetupCompletedKey(email: String) = booleanPreferencesKey("user_${userKey(email)}_biometric_setup_completed")
        private fun profileImageUriKey(email: String) = stringPreferencesKey("user_${userKey(email)}_profile_image_uri")

        // OFFLINE-FIRST: Nueva llave para guardar el estado de sincronización del perfil
        private fun isProfileSyncedKey(email: String) = booleanPreferencesKey("user_${userKey(email)}_is_profile_synced")
    }
}
