package org.weresomeware.tx.txmax.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

// Fallback colors for Android 11 and below
private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()

@Composable
fun TxMaxTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tx_max_settings", Context.MODE_PRIVATE) }

    // Check what the Android OS is currently set to
    val systemInDarkTheme = isSystemInDarkTheme()

    // Live State for Dark Mode (Defaults to System Theme if user hasn't set it manually)
    var isDarkTheme by remember {
        mutableStateOf(
            if (prefs.contains("dark_mode")) {
                prefs.getBoolean("dark_mode", false)
            } else {
                systemInDarkTheme
            }
        )
    }

    // Live State for Font Size
    var fontSizePref by remember { mutableStateOf(prefs.getString("font_size", "Normal") ?: "Normal") }

    // Listener to instantly update the UI when settings change
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == "dark_mode") isDarkTheme = sharedPrefs.getBoolean("dark_mode", false)
            if (key == "font_size") fontSizePref = sharedPrefs.getString("font_size", "Normal") ?: "Normal"
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // Select Colors (Uses System Dynamic Colors on Android 12+, otherwise uses fallbacks)
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Calculate Font Scaling Factor
    val fontScale = when (fontSizePref) {
        "Small" -> 0.85f
        "Large" -> 1.15f
        "Extra Large" -> 1.30f
        else -> 1.0f // Normal
    }

    // Apply the scaling to your Typography
    val baseTypography = Typography()
    val scaledTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontScale),
        displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontScale),
        displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontScale),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontScale),
        titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontScale),
        titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontScale),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontScale),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontScale),
        labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontScale)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}