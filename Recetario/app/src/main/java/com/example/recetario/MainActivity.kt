package com.example.recetario

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.recetario.navigation.AppNavigation
import com.example.recetario.ui.theme.RecetarioTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecetarioTheme {
                AppNavigation()
            }
        }
    }
}