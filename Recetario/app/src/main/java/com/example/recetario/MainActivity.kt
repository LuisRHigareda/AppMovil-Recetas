package com.example.recetario

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.recetario.navigation.AppNavigation
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.worker.SyncManager

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ¡Red de seguridad! Al abrir la app, encolamos una revisión de sincronización.
        // Usamos applicationContext para que el Worker no dependa de si la Actividad se cierra.
        SyncManager.scheduleSync(applicationContext)

        setContent {
            RecetarioTheme {
                AppNavigation()
            }
        }
    }
}