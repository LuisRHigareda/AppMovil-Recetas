package com.example.recetario.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recetario.data.AuthRepository
import com.example.recetario.data.firebase.FirebaseDataSource
import com.example.recetario.data.firebase.UserFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class UserProfileWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val authRepository = AuthRepository(applicationContext)
                val firebaseDataSource = FirebaseDataSource()

                // 1. Obtenemos el usuario actual
                val currentUser = authRepository.userPreferencesFlow.first()

                // 2. Revisamos si hay sesión activa, si el email existe y si NO está sincronizado
                if (currentUser.email.isNotBlank() && !currentUser.isProfileSynced) {

                    // 3. Convertimos las preferencias locales al formato de Firebase
                    val userFirebase = UserFirebase(
                        email = currentUser.email,
                        firstName = currentUser.firstName,
                        lastName = currentUser.lastName,
                        birthDate = currentUser.birthDate,
                        gender = currentUser.gender,
                        profileImageUri = currentUser.profileImageUri,
                        passwordHash = currentUser.passwordHash
                    )

                    // 4. Intentamos subirlo a la nube
                    val success = firebaseDataSource.saveUserProfile(userFirebase)

                    if (success) {
                        // 5. ¡Éxito! Le decimos a DataStore que ya está todo respaldado
                        authRepository.markProfileAsSynced(currentUser.email)
                    } else {
                        // Si no hay internet, falla el guardado y le decimos que reintente después
                        return@withContext Result.retry()
                    }
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}