package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG_AUTH = "AuthException"

class NoGoogleAccountException(message: String) : Exception(message)
class GoogleSignInLauncherRequiredException(val intent: Intent) : Exception("Google Sign-In Intent Required")

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

    private fun parseFirebaseAuthException(e: FirebaseAuthException): Exception {
        val errorCode = e.errorCode.uppercase()
        val localizedMsg = e.localizedMessage ?: ""
        Log.e(TAG_AUTH, "FirebaseAuthException [Code: $errorCode]: $localizedMsg", e)

        val userMsg = when {
            errorCode.contains("INVALID_EMAIL") || errorCode.contains("INVALID-EMAIL") ->
                "عنوان البريد الإلكتروني غير صحيح أو بصيغة غير صالحة."
            errorCode.contains("WRONG_PASSWORD") || errorCode.contains("WRONG-PASSWORD") || errorCode.contains("INVALID_CREDENTIAL") ->
                "كلمة المرور غير صحيحة. يرجى التأكد وإعادة المحاولة."
            errorCode.contains("USER_NOT_FOUND") || errorCode.contains("USER-NOT-FOUND") ->
                "لا يوجد حساب مسجل بهذا البريد الإلكتروني."
            errorCode.contains("EMAIL_ALREADY_IN_USE") || errorCode.contains("EMAIL-ALREADY-IN-USE") ->
                "البريد الإلكتروني مستخدم بالفعل بحساب آخر."
            errorCode.contains("WEAK_PASSWORD") || errorCode.contains("WEAK-PASSWORD") ->
                "كلمة المرور ضعيفة جداً. يجب أن تحتوي على 6 أحرف على الأقل."
            errorCode.contains("USER_DISABLED") || errorCode.contains("USER-DISABLED") ->
                "تم تعطيل هذا الحساب بطلب من إدارة النظام."
            errorCode.contains("TOO_MANY_REQUESTS") || errorCode.contains("TOO-MANY-REQUESTS") ->
                "تم حظر المحاولات مؤقتاً بسبب كثرة المحاولات الخاطئة. يرجى الانتظار قليلاً."
            errorCode.contains("NETWORK_REQUEST_FAILED") || errorCode.contains("NETWORK-REQUEST-FAILED") ->
                "تعذر الاتصال بالشبكة. يرجى التأكد من توفر اتصال بالإنترنت."
            errorCode.contains("ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") ->
                "يوجد حساب بالفعل مرتبط ببيانات اعتماد مختلفة."
            errorCode.contains("OPERATION_NOT_ALLOWED") ->
                "طريقة تسجيل الدخول هذه غير مفعلة حالياً."
            else -> "فشل تسجيل الدخول: ${localizedMsg.ifBlank { errorCode }}"
        }
        return Exception(userMsg)
    }

    fun getGoogleSignInIntent(context: Context): Intent? {
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "CRITICAL: Unable to resolve R.string.default_web_client_id from resources", e)
            return null
        }
        if (serverClientId.isBlank()) {
            Log.e(TAG_AUTH, "CRITICAL: R.string.default_web_client_id is empty!")
            return null
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        val act = context.findActivity() ?: context
        return GoogleSignIn.getClient(act, gso).signInIntent
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "CRITICAL: Unable to resolve R.string.default_web_client_id from resources", e)
            return@withContext Result.failure(Exception("خطأ تكوين النظام: تعذر الوصول لمعرف العميل (default_web_client_id)"))
        }

        if (serverClientId.isBlank()) {
            Log.e(TAG_AUTH, "CRITICAL: R.string.default_web_client_id is empty!")
            return@withContext Result.failure(Exception("خطأ تكوين النظام: معرف عميل Google فارغ"))
        }

        Log.i(TAG_AUTH, "Starting Google Sign-In with Web Client ID: $serverClientId")

        val activityContext = context.findActivity() ?: run {
            Log.w(TAG_AUTH, "Context is not an Activity, using original context: $context")
            context
        }

        // 1. Primary Attempt: Jetpack Credential Manager
        try {
            Log.d(TAG_AUTH, "Executing CredentialManager request with GetGoogleIdOption...")
            val credentialManager = CredentialManager.create(activityContext)

            val getGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
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
                    return@withContext Result.success(user)
                }
            } else {
                Log.e(TAG_AUTH, "Extracted ID Token was null or empty from CredentialManager response")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG_AUTH, "User cancelled Google Sign-In prompt", e)
            return@withContext Result.failure(Exception("تم إلغاء عملية تسجيل الدخول بواسطة المستخدم"))
        } catch (e: NoCredentialException) {
            Log.e(TAG_AUTH, "NoCredentialException in CredentialManager: ${e.message}", e)
        } catch (e: GetCredentialException) {
            Log.e(TAG_AUTH, "GetCredentialException: type=${e.type}, message=${e.message}", e)
        } catch (e: FirebaseAuthException) {
            return@withContext Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Unexpected exception during CredentialManager sign-in: ${e::class.java.simpleName}: ${e.message}", e)
        }

        // 2. Secondary Fallback: Launch GoogleSignIn Activity Intent
        Log.d(TAG_AUTH, "CredentialManager did not return token. Fallback to GoogleSignIn Activity Intent...")
        val signInIntent = getGoogleSignInIntent(context)
        if (signInIntent != null) {
            return@withContext Result.failure(GoogleSignInLauncherRequiredException(signInIntent))
        }

        Log.e(TAG_AUTH, "FATAL: Unable to create Google Sign-In Intent.")
        Result.failure(
            Exception("تعذر إكمال تسجيل الدخول بواسطة حساب Google. يرجى التأكد من تفعيل خدمات Google Play وتوافر اتصال بالشبكة.")
        )
    }

    suspend fun signInWithGoogleIntentResult(intentData: Intent?): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        if (intentData == null) {
            return@withContext Result.failure(Exception("تم إلغاء عملية تسجيل الدخول بواسطة المستخدم"))
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intentData)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (!idToken.isNullOrBlank()) {
                Log.i(TAG_AUTH, "Successfully retrieved ID token via GoogleSignIn intent result. Authenticating with Firebase...")
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Log.i(TAG_AUTH, "Firebase Google Auth SUCCESSFUL! UID: ${user.uid}, Email: ${user.email}")
                    return@withContext Result.success(user)
                }
            }
            Log.e(TAG_AUTH, "Extracted ID token was null or empty from GoogleSignIn intent result")
            Result.failure(Exception("تعذر الحصول على رمز الهوية لحساب Google."))
        } catch (e: ApiException) {
            Log.e(TAG_AUTH, "ApiException in GoogleSignIn intent result: statusCode=${e.statusCode}, message=${e.message}", e)
            val userMsg = when (e.statusCode) {
                12501 -> "تم إلغاء عملية تسجيل الدخول بواسطة المستخدم."
                12500 -> "خطأ في مصادقة Google (رمز 12500). يرجى التأكد من إعدادات الحساب."
                4 -> "يتطلب تسجيل الدخول اختيار حساب Google من القائمة."
                7 -> "تعذر الاتصال بخدمات Google. يرجى التأكد من اتصال الإنترنت."
                else -> "فشل تسجيل الدخول بواسطة Google (رمز الخطأ: ${e.statusCode})"
            }
            Result.failure(Exception(userMsg))
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Unexpected error in GoogleSignIn intent result: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر إكمال تسجيل الدخول بواسطة Google."))
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            return@withContext Result.failure(Exception("يرجى إدخال البريد الإلكتروني وكلمة المرور."))
        }

        return@withContext try {
            val user = withTimeout(20_000L) {
                val result = auth.signInWithEmailAndPassword(cleanEmail, pass).await()
                result.user ?: throw IllegalStateException("بيانات المستخدم غير صالحة")
            }
            Log.i(TAG_AUTH, "Email sign-in SUCCESSFUL! UID: ${user.uid}, Email: ${user.email}")
            Result.success(user)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Email sign-in failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل تسجيل الدخول، تحقق من البريد الإلكتروني وكلمة المرور."))
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            return@withContext Result.failure(Exception("يرجى إدخال البريد الإلكتروني وكلمة المرور."))
        }
        if (pass.length < 6) {
            return@withContext Result.failure(Exception("كلمة المرور قصيرة جداً. يجب أن تحتوي على 6 أحرف على الأقل."))
        }

        return@withContext try {
            val user = withTimeout(20_000L) {
                val result = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                val createdUser = result.user ?: throw IllegalStateException("فشل إنشاء الحساب")
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    this.displayName = displayName.ifBlank { "طالب SCOPE" }
                }
                createdUser.updateProfile(profileUpdates).await()
                createdUser
            }
            Log.i(TAG_AUTH, "Email sign-up SUCCESSFUL! UID: ${user.uid}, Email: ${user.email}")
            Result.success(user)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Email sign-up failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل إنشاء الحساب، يرجى المحاولة لاحقاً."))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return@withContext Result.failure(Exception("يرجى إدخال البريد الإلكتروني الخاص بحسابك."))
        }

        return@withContext try {
            withTimeout(20_000L) {
                auth.sendPasswordResetEmail(cleanEmail).await()
            }
            Log.i(TAG_AUTH, "Password reset email sent to $cleanEmail")
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Password reset failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "فشل إرسال رابط استعادة كلمة المرور."))
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(Exception("المستخدم غير مسجل الدخول"))
        if (newPassword.isBlank() || newPassword.length < 6) {
            return@withContext Result.failure(Exception("كلمة المرور الجديدة يجب أن تكون 6 أحرف على الأقل."))
        }

        return@withContext try {
            withTimeout(20_000L) {
                user.updatePassword(newPassword).await()
            }
            Log.i(TAG_AUTH, "Password updated successfully for UID: ${user.uid}")
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Password update failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر تغيير كلمة المرور."))
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(Exception("المستخدم غير مسجل الدخول"))
        return@withContext try {
            withTimeout(15_000L) {
                user.sendEmailVerification().await()
            }
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Send email verification failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر إرسال بريد التفعيل."))
        }
    }

    suspend fun reloadUserAndCheckVerified(): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(Exception("المستخدم غير مسجل الدخول"))
        return@withContext try {
            withTimeout(15_000L) {
                user.reload().await()
            }
            Result.success(user.isEmailVerified)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Reload user verification failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر التحقق من حالة الحساب."))
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        return@withContext try {
            val user = withTimeout(20_000L) {
                val result = auth.signInAnonymously().await()
                result.user ?: throw IllegalStateException("فشل تسجيل الدخول المجهول (Guest Auth)")
            }
            Log.i(TAG_AUTH, "Anonymous sign-in SUCCESSFUL! UID: ${user.uid}")
            Result.success(user)
        } catch (e: FirebaseAuthException) {
            Result.failure(parseFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG_AUTH, "Anonymous sign-in failed: ${e.message}", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر الدخول كزائر، يرجى المحاولة لاحقاً."))
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
