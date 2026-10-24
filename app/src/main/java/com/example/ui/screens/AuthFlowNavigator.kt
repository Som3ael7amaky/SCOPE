package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun AuthFlowNavigator(
    isSigningIn: Boolean,
    errorMessage: String?,
    context: Context,
    onSignInWithGoogle: () -> Unit,
    onContinueAsGuest: () -> Unit,
    // Kept for backward compatibility with existing callers
    onSignInWithEmail: (String, String) -> Unit = { _, _ -> },
    onRegisterWithEmail: (email: String, pass: String, name: String, scopeId: String, uni: String, major: String, year: String, phone: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onSendPasswordReset: (String) -> Unit = {}
) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
    } else {
        SignInScreen(
            isSigningIn = isSigningIn,
            errorMessage = errorMessage,
            onSignInWithGoogle = onSignInWithGoogle,
            onContinueAsGuest = onContinueAsGuest
        )
    }
}
