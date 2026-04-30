package com.example.recetario.data

import android.content.Context
import java.security.MessageDigest
import java.util.Locale
import com.example.recetario.model.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class AuthRepository(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    // Instanciamos nuestra Capa 2
    private val remoteDataSource: UserDataSource = FirestoreUserDataSource()

    private val preferences = context.applicationContext.getSharedPreferences(
        "recetario_auth_preferences",
        Context.MODE_PRIVATE
    )

    // --- REGISTRO ---
    suspend fun registerUser(user: User): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            val uid = authResult.user?.uid ?: throw Exception("ID no generado")

            val finalizedUser = user.copy(id = uid)

            // 1. Guardar Local
            saveUserLocally(finalizedUser)
            // 2. Guardar en Nube
            remoteDataSource.saveUser(finalizedUser)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGIN CON FIREBASE (NUEVO) ---
    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            // 1. Validar credenciales con Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Usuario no encontrado")

            // 2. Descargar el perfil desde Firestore
            val remoteResult = remoteDataSource.getUser(uid)
            val user = remoteResult.getOrNull()

            if (user != null) {
                // Le reasignamos el password temporalmente solo para generar el hash local
                // (Recuerda que de Firestore baja vacío por el @get:Exclude)
                val userWithPassword = user.copy(password = password)
                saveUserLocally(userWithPassword)

                Result.success(Unit)
            } else {
                Result.failure(Exception("No se encontraron los datos del perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- OBTENER USUARIO ACTUAL ---
    suspend fun getCurrentUser(): Result<User?> {
        val uid = auth.currentUser?.uid ?: return Result.success(null)

        val remoteResult = remoteDataSource.getUser(uid)
        remoteResult.getOrNull()?.let {
            // Guardamos localmente pero sin sobreescribir el hash del password
            saveUserLocally(it)
        }

        return remoteResult
    }

    // --- LOGINS LOCALES (Para Room/SharedPreferences y Huella) ---
    fun hasRegisteredUser(): Boolean {
        return preferences.getString(KEY_EMAIL, null) != null
    }

    fun login(email: String, password: String): Boolean {
        val savedEmail = preferences.getString(KEY_EMAIL, null)
        val savedPasswordHash = preferences.getString(KEY_PASSWORD_HASH, null)

        val isValid = savedEmail == email.trim().lowercase() &&
                savedPasswordHash == hashPassword(password)

        if (isValid) setSessionActive(true)
        return isValid
    }

    fun loginWithSavedUser(password: String): Boolean {
        val savedPasswordHash = preferences.getString(KEY_PASSWORD_HASH, null)
        val isValid = savedPasswordHash == hashPassword(password)

        if (isValid) setSessionActive(true)
        return isValid
    }

    // --- ACTUALIZAR Y BORRAR ---
    suspend fun updateUserProfile(user: User): Result<Unit> {
        saveUserLocally(user)
        return remoteDataSource.updateUser(user)
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("No hay sesión activa")

            remoteDataSource.deleteUser(uid)
            auth.currentUser?.delete()?.await()
            clearLocalData()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- UTILIDADES LOCALES CORREGIDAS ---
    private fun saveUserLocally(user: User) {
        preferences.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_FIRST_NAME, user.firstName.trim())
            putString(KEY_LAST_NAME, user.lastName.trim())
            putString(KEY_BIRTH_DATE, user.birthDate.trim())
            putString(KEY_GENDER, user.gender.trim())
            putString(KEY_EMAIL, user.email.trim().lowercase())
            putBoolean(KEY_SESSION_ACTIVE, true)

            // Solo creamos el hash si el usuario trae contraseña
            if (user.password.isNotEmpty()) {
                putString(KEY_PASSWORD_HASH, hashPassword(user.password))
            }
            apply()
        }
    }

    private fun clearLocalData() {
        preferences.edit().clear().apply()
    }

    fun setProfileImageUri(uri: String?) {
        preferences.edit().putString(KEY_PROFILE_IMAGE_URI, uri ?: "").apply()
    }

    fun getProfileImageUri(): String? {
        return preferences.getString(KEY_PROFILE_IMAGE_URI, "")?.ifBlank { null }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setSessionActive(active: Boolean) {
        preferences.edit().putBoolean(KEY_SESSION_ACTIVE, active).apply()
    }

    fun isSessionActive(): Boolean {
        return preferences.getBoolean(KEY_SESSION_ACTIVE, false)
    }

    fun logout() {
        preferences.edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
        auth.signOut() // <- Agregado para cerrar sesión en Firebase también
    }

    fun getUserId(): String {
        return preferences.getString(KEY_USER_ID, "") ?: ""
    }
    fun getFirstName(): String = preferences.getString(KEY_FIRST_NAME, "") ?: ""
    fun getLastName(): String = preferences.getString(KEY_LAST_NAME, "") ?: ""
    fun getBirthDate(): String = preferences.getString(KEY_BIRTH_DATE, "") ?: ""
    fun getGender(): String = preferences.getString(KEY_GENDER, "") ?: ""
    fun getEmail(): String = preferences.getString(KEY_EMAIL, "") ?: ""

    fun getFullName(): String {
        val fullName = "${getFirstName()} ${getLastName()}".trim()
        return fullName.ifEmpty { "Usuario" }
    }

    fun getInitials(): String {
        val firstInitial = getFirstName().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val lastInitial = getLastName().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return "$firstInitial$lastInitial".ifBlank { "U" }.uppercase(Locale.getDefault())
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_GENDER = "gender"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SESSION_ACTIVE = "session_active"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
    }
}