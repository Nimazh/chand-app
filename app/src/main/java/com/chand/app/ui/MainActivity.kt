package com.chand.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.chand.app.ui.screen.MainScreen
import com.chand.app.ui.screen.MainViewModel
import com.chand.app.ui.screen.SettingsScreen
import com.chand.app.ui.theme.ChandTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.appThemeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            DisposableEffect(isDark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
                onDispose { }
            }

            ChandTheme(darkTheme = isDark) {
                var currentScreen by rememberSaveable { mutableStateOf("main") }
                BackHandler(enabled = currentScreen != "main") { currentScreen = "main" }

                when (currentScreen) {
                    "main" -> MainScreen(
                        viewModel = viewModel,
                        onOpenSettings = { currentScreen = "settings" }
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "main" }
                    )
                }
            }
        }
    }

}
