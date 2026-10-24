package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private enum class AuthScreenState {
    SPLASH,
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthFlowNavigator(
    isSigningIn: Boolean,
    errorMessage: String?,
    context: Context,
    onSignInWithGoogle: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit = { _, _ -> },
    onRegisterWithEmail: (email: String, pass: String, name: String, scopeId: String, uni: String, major: String, year: String, phone: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onSendPasswordReset: (email: String) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(AuthScreenState.SPLASH) }

    when (currentScreen) {
        AuthScreenState.SPLASH -> {
            SplashScreen(
                onTimeout = { currentScreen = AuthScreenState.SIGN_IN }
            )
        }
        AuthScreenState.SIGN_IN -> {
            SignInScreen(
                isSigningIn = isSigningIn,
                errorMessage = errorMessage,
                onSignInWithGoogle = onSignInWithGoogle,
                onContinueAsGuest = onContinueAsGuest,
                onSignInWithEmail = onSignInWithEmail,
                onSendPasswordReset = onSendPasswordReset,
                onNavigateToSignUp = { currentScreen = AuthScreenState.SIGN_UP }
            )
        }
        AuthScreenState.SIGN_UP -> {
            SignUpScreen(
                isSigningIn = isSigningIn,
                errorMessage = errorMessage,
                onRegisterWithEmail = onRegisterWithEmail,
                onSignInWithGoogle = onSignInWithGoogle,
                onNavigateToSignIn = { currentScreen = AuthScreenState.SIGN_IN },
                onBack = { currentScreen = AuthScreenState.SIGN_IN }
            )
        }
    }
}
