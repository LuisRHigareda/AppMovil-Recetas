package com.example.recetario.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recetario.data.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

// Usamos CoroutineWorker porque nuestra sincronización usa funciones 'suspend'
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Instanciamos tu gerente (el repositorio)
                // applicationContext se lo pasa el mismo WorkManager
                val repository = RecipeRepository(applicationContext)

                // 2. ¡Llamamos a tu función de rescate!
                repository.syncAllPendingData()

                // 3. Le decimos a Android que el trabajo fue un éxito
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla por alguna razón (ej. el internet se cayó a la mitad),
                // le decimos que reintente más tarde.
                Result.retry()
            }
        }
    }
}



object SyncManager {
    fun scheduleSync(context: Context) {
        // 1. Definimos las restricciones: ¡SOLO ejecutar si hay internet!
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 2. Preparamos la petición del trabajo
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        // 3. Lo metemos a la cola del sistema
        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}