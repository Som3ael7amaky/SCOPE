package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseUser

enum class AuthStep {
    SPLASH,
    WELCOME,
    SIGN_IN,
    SIGN_UP,
    EMAIL_VERIFICATION,
    VERIFICATION_SUCCESS
}

@Composable
fun AuthFlowNavigator(
    isSigningIn: Boolean,
    errorMessage: String?,
    context: Context,
    onSignInWithGoogle: () -> Unit,
    onSignInWithEmail: (String, String) -> Unit,
    onRegisterWithEmail: (email: String, pass: String, name: String, scopeId: String, uni: String, major: String, year: String, phone: String) -> Unit,
    onSendPasswordReset: (String) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var currentStep by remember { mutableStateOf(AuthStep.SPLASH) }
    var registeredEmail by remember { mutableStateOf("") }

    when (currentStep) {
        AuthStep.SPLASH -> {
            SplashScreen(
                onTimeout = { currentStep = AuthStep.WELCOME }
            )
        }
        AuthStep.WELCOME -> {
            WelcomeScreen(
                onNavigateToSignUp = { currentStep = AuthStep.SIGN_UP },
                onNavigateToSignIn = { currentStep = AuthStep.SIGN_IN },
                onContinueAsGuest = onContinueAsGuest,
                onSkip = onContinueAsGuest
            )
        }
        AuthStep.SIGN_IN -> {
            SignInScreen(
                isSigningIn = isSigningIn,
                errorMessage = errorMessage,
                onSignInWithGoogle = onSignInWithGoogle,
                onSignInWithEmail = onSignInWithEmail,
                onRegisterWithEmail = onRegisterWithEmail,
                onSendPasswordReset = onSendPasswordReset,
                onLookupEmailByIdentifier = { _, callback -> callback(null) },
                onContinueAsGuest = onContinueAsGuest,
                onNavigateToSignUp = { currentStep = AuthStep.SIGN_UP },
                onBack = { currentStep = AuthStep.WELCOME }
            )
        }
        AuthStep.SIGN_UP -> {
            SignUpScreen(
                isSigningIn = isSigningIn,
                errorMessage = errorMessage,
                onRegisterWithEmail = { email, pass, name, scopeId, uni, major, year, phone ->
                    registeredEmail = email
                    onRegisterWithEmail(email, pass, name, scopeId, uni, major, year, phone)
                    currentStep = AuthStep.EMAIL_VERIFICATION
                },
                onSignInWithGoogle = onSignInWithGoogle,
                onNavigateToSignIn = { currentStep = AuthStep.SIGN_IN },
                onBack = { currentStep = AuthStep.WELCOME }
            )
        }
        AuthStep.EMAIL_VERIFICATION -> {
            EmailVerificationScreen(
                userEmail = registeredEmail,
                onVerified = { currentStep = AuthStep.VERIFICATION_SUCCESS },
                onBack = { currentStep = AuthStep.SIGN_UP }
            )
        }
        AuthStep.VERIFICATION_SUCCESS -> {
            VerificationSuccessScreen(
                onGoToApp = onContinueAsGuest // Or proceeds into app once Firebase auth state updates
            )
        }
    }
}
