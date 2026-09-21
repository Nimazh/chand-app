package com.chand.app.ui

import android.os.Build
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

        enableMaxDisplayRefreshRate()

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
                var currentScreen by remember { mutableStateOf("main") }

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

    override fun onResume() {
        super.onResume()
        enableMaxDisplayRefreshRate()
    }

    /**
     * Unlocks and requests the highest available display refresh rate (e.g. 144Hz, 120Hz, 90Hz)
     * supported by the device's screen panel, ensuring ultra-smooth 144Hz scrolling and animations.
     */
    private fun enableMaxDisplayRefreshRate() {
        try {
            val activeDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }

            val supportedModes = activeDisplay?.supportedModes ?: emptyArray()
            if (supportedModes.isNotEmpty()) {
                // Find highest available refresh rate (e.g. 144Hz, 120Hz, 90Hz)
                val maxRefreshRate = supportedModes.maxOfOrNull { it.refreshRate } ?: 60f

                // Select mode with highest refresh rate and best resolution
                val bestMode = supportedModes
                    .filter { it.refreshRate >= maxRefreshRate - 0.5f }
                    .maxByOrNull { it.physicalWidth * it.physicalHeight }
                    ?: supportedModes.maxByOrNull { it.refreshRate }

                if (bestMode != null) {
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = bestMode.modeId
                    layoutParams.preferredRefreshRate = bestMode.refreshRate
                    window.attributes = layoutParams
                }
            }
        } catch (_: Throwable) {
            // Graceful fallback if OEM restricted
        }
    }
}
