package com.example.leaf.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FixedColorScheme = lightColorScheme(
    primary = Color(0xFF1B5E20), 
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7), 
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF2E7D32), 
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9), 
    onSecondaryContainer = Color(0xFF2E7D32),
    tertiary = Color(0xFF4CAF50), 
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F5E9), 
    onTertiaryContainer = Color(0xFF4CAF50),
    background = Color(0xFFF5F5F5), 
    onBackground = Color(0xFF1B1B1B),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1B), 
    surfaceVariant = Color(0xFFEEEEEE), 
    onSurfaceVariant = Color(0xFF1B1B1B),
    error = Color(0xFFB00020), 
    onError = Color.White,
    errorContainer = Color(0xFFFCDAD7), 
    onErrorContainer = Color(0xFFB00020)
)

@Composable
fun LeafTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FixedColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}