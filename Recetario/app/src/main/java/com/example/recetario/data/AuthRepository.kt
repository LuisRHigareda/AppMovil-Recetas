package com.example.recetario.data

import android.content.Context
import java.security.MessageDigest

class AuthRepository(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        "recetario_auth_preferences",
        Context.MODE_PRIVATE
    )

    fun registerUser(
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        email: String,
        password: String
    ) {
        preferences.edit()
            .putString(KEY_FIRST_NAME, firstName.trim())
            .putString(KEY_LAST_NAME, lastName.trim())
            .putString(KEY_BIRTH_DATE, birthDate.trim())
            .putString(KEY_GENDER, gender.trim())
            .putString(KEY_EMAIL, email.trim().lowercase())
            .putString(KEY_PASSWORD_HASH, hashPassword(password))
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .apply()
    }

    fun hasRegisteredUser(): Boolean {
        return preferences.getString(KEY_EMAIL, null) != null
    }

    fun login(email: String, password: String): Boolean {
        val savedEmail = preferences.getString(KEY_EMAIL, null)
        val savedPasswordHash = preferences.getString(KEY_PASSWORD_HASH, null)

        val isValid = savedEmail == email.trim().lowercase() &&
                savedPasswordHash == hashPassword(password)

        if (isValid) {
            setSessionActive(true)
        }

        return isValid
    }

    fun loginWithSavedUser(password: String): Boolean {
        val savedPasswordHash = preferences.getString(KEY_PASSWORD_HASH, null)

        val isValid = savedPasswordHash == hashPassword(password)

        if (isValid) {
            setSessionActive(true)
        }

        return isValid
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    fun isBiometricEnabled(): Boolean {
        return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setSessionActive(active: Boolean) {
        preferences.edit()
            .putBoolean(KEY_SESSION_ACTIVE, active)
            .apply()
    }

    fun isSessionActive(): Boolean {
        return preferences.getBoolean(KEY_SESSION_ACTIVE, false)
    }

    fun logout() {
        preferences.edit()
            .putBoolean(KEY_SESSION_ACTIVE, false)
            .apply()
    }

    fun getFullName(): String {
        val firstName = preferences.getString(KEY_FIRST_NAME, "") ?: ""
        val lastName = preferences.getString(KEY_LAST_NAME, "") ?: ""

        val fullName = "$firstName $lastName".trim()

        return fullName.ifEmpty {
            "Usuario"
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_GENDER = "gender"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SESSION_ACTIVE = "session_active"
    }
}