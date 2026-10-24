package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ScopePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = ScopePrimaryContainer,
    onPrimaryContainer = ScopeOnPrimaryContainer,
    secondary = ScopeSecondaryDark,
    onSecondary = ScopeBgDark,
    secondaryContainer = ScopeSecondaryContainer,
    onSecondaryContainer = ScopeOnSecondaryContainer,
    tertiary = ScopeAccent,
    background = ScopeBgDark,
    onBackground = ScopeTextMainDark,
    surface = ScopeSurfaceDark,
    onSurface = ScopeTextMainDark,
    surfaceVariant = ScopeSurfaceVariantDark,
    onSurfaceVariant = ScopeTextSecondaryDark,
    outline = ScopeOutlineDark,
    error = ScopeError
)

private val LightColorScheme = lightColorScheme(
    primary = ScopePrimary,
    onPrimary = ScopeSurfaceLight,
    primaryContainer = ScopePrimaryContainer,
    onPrimaryContainer = ScopeOnPrimaryContainer,
    secondary = ScopeSecondary,
    onSecondary = ScopeSurfaceLight,
    secondaryContainer = ScopeSecondaryContainer,
    onSecondaryContainer = ScopeOnSecondaryContainer,
    tertiary = ScopeAccent,
    background = ScopeBgLight,
    onBackground = ScopeTextMainLight,
    surface = ScopeSurfaceLight,
    onSurface = ScopeTextMainLight,
    surfaceVariant = ScopeSurfaceVariantLight,
    onSurfaceVariant = ScopeTextSecondaryLight,
    outline = ScopeOutlineLight,
    error = ScopeError
)

@Composable
fun ScopeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScopeTypography,
        content = content
    )
}
