package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private const val TAG_AUTH = "AuthException"

class NoGoogleAccountException(message: String) : Exception(message)

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "CRITICAL: Unable to resolve R.string.default_web_client_id from resources", e)
            return Result.failure(Exception("خطأ تكوين النظام: تعذر الوصول لمعرف العميل (default_web_client_id)"))
        }

        if (serverClientId.isBlank()) {
            Log.e(TAG_AUTH, "CRITICAL: R.string.default_web_client_id is empty!")
            return Result.failure(Exception("خطأ تكوين النظام: معرف عميل Google فارغ"))
        }

        Log.i(TAG_AUTH, "Starting Google Sign-In with Web Client ID: $serverClientId")

        val activityContext = context.findActivity() ?: run {
            Log.w(TAG_AUTH, "Context is not an Activity, using original context: $context")
            context
        }

        // 1. Primary Attempt: Jetpack Credential Manager
        try {
            Log.d(TAG_AUTH, "Executing CredentialManager request...")
            val credentialManager = CredentialManager.create(activityContext)

            val getGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val getSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .addCredentialOption(getSignInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(context = activityContext, request = request)
            val credential = result.credential

            Log.i(TAG_AUTH, "CredentialManager returned credential of type: ${credential.type}")

            val idToken: String? = when {
                credential is CustomCredential && (
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                ) -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        Log.e(TAG_AUTH, "GoogleIdTokenCredential.createFrom parsing error: ${e.message}", e)
                        credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: credential.data.getString("id_token")
                    }
                }
                else -> {
                    Log.w(TAG_AUTH, "Credential is not standard CustomCredential type: ${credential.type}")
                    (credential as? CustomCredential)?.data?.let { bundle ->
                        bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: bundle.getString("id_token")
                    }
                }
            }

            if (!idToken.isNullOrBlank()) {
                Log.i(TAG_AUTH, "Successfully retrieved ID Token via CredentialManager. Authenticating with Firebase...")
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Log.i(TAG_AUTH, "Firebase Authentication SUCCESSFUL! UID: ${user.uid}, Email: ${user.email}")
                    return Result.success(user)
                } else {
                    Log.e(TAG_AUTH, "Firebase signInWithCredential returned null user object")
                }
            } else {
                Log.e(TAG_AUTH, "Extracted ID Token was null or empty from CredentialManager response")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG_AUTH, "User cancelled Google Sign-In prompt", e)
            return Result.failure(Exception("تم إلغاء عملية تسجيل الدخول بواسطة المستخدم"))
        } catch (e: NoCredentialException) {
            Log.e(TAG_AUTH, "NoCredentialException in CredentialManager: ${e.message}", e)
        } catch (e: GetCredentialException) {
            Log.e(TAG_AUTH, "GetCredentialException: type=${e.type}, message=${e.message}", e)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG_AUTH, "FirebaseAuthException during Google auth: code=${e.errorCode}, message=${e.message}", e)
            return Result.failure(Exception("فشل مصادقة Firebase: ${e.localizedMessage ?: e.errorCode}"))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Unexpected exception during CredentialManager sign-in: ${e::class.java.simpleName}: ${e.message}", e)
        }

        // 2. Secondary Fallback: GoogleSignInClient (Play Services Auth)
        val act = context.findActivity()
        if (act != null) {
            try {
                Log.d(TAG_AUTH, "Attempting fallback via GoogleSignInClient...")
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(serverClientId)
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(act, gso)
                val accountTask = googleSignInClient.silentSignIn()
                val account = if (accountTask.isSuccessful) {
                    accountTask.result
                } else {
                    try {
                        accountTask.await()
                    } catch (e: Exception) {
                        Log.e(TAG_AUTH, "GoogleSignInClient silentSignIn failed: ${e.message}", e)
                        null
                    }
                }

                if (account?.idToken != null) {
                    Log.i(TAG_AUTH, "Successfully retrieved ID token via GoogleSignInClient fallback")
                    val authCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        Log.i(TAG_AUTH, "Firebase fallback authentication SUCCESSFUL! UID: ${user.uid}")
                        return Result.success(user)
                    }
                }
            } catch (e: ApiException) {
                Log.e(TAG_AUTH, "ApiException in GoogleSignInClient fallback: statusCode=${e.statusCode}, message=${e.message}", e)
            } catch (e: Exception) {
                Log.e(TAG_AUTH, "Unexpected error in GoogleSignInClient fallback: ${e.message}", e)
            }
        }

        Log.e(TAG_AUTH, "FATAL: All Google Sign-In attempts failed. Returning error result.")
        return Result.failure(
            Exception("تعذر إكمال تسجيل الدخول بواسطة حساب Google. يرجى التأكد من اختيار حساب ووجود اتصال بالشبكة.")
        )
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val user = withTimeout(20_000L) {
                val result = auth.signInWithEmailAndPassword(email.trim().lowercase(), pass).await()
                result.user ?: throw IllegalStateException("بيانات المستخدم غير صالحة")
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Email sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val user = withTimeout(20_000L) {
                val result = auth.createUserWithEmailAndPassword(email.trim().lowercase(), pass).await()
                val createdUser = result.user ?: throw IllegalStateException("فشل إنشاء المستخدم")
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    this.displayName = displayName.ifBlank { "طالب SCOPE" }
                }
                createdUser.updateProfile(profileUpdates).await()
                createdUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Email sign-up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            withTimeout(20_000L) {
                auth.sendPasswordResetEmail(email.trim().lowercase()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Password reset failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("المستخدم غير مسجل الدخول"))
            withTimeout(20_000L) {
                user.updatePassword(newPassword).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Password update failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("المستخدم غير مسجل الدخول"))
            withTimeout(15_000L) {
                user.sendEmailVerification().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Send email verification failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun reloadUserAndCheckVerified(): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("المستخدم غير مسجل الدخول"))
            withTimeout(15_000L) {
                user.reload().await()
            }
            Result.success(user.isEmailVerified)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Reload user verification failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val user = withTimeout(20_000L) {
                val result = auth.signInAnonymously().await()
                result.user ?: throw IllegalStateException("فشل تسجيل الدخول المجهول (Guest Auth)")
            }
            Log.i(TAG_AUTH, "Anonymous sign-in SUCCESSFUL! UID: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Anonymous sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
