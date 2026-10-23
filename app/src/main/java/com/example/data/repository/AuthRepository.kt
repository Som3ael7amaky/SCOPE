package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

class NoGoogleAccountException(message: String) : Exception(message)

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
        return try {
            val operationResult = withTimeout(20_000L) {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = context.getString(R.string.default_web_client_id)

                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user ?: throw IllegalStateException("Firebase user was null after Google sign-in")
                } else {
                    throw IllegalStateException("استجابة غير متوقعة من مزود هوية Google")
                }
            }
            Result.success(operationResult)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("AuthRepository", "Google sign-in timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالخادم، يرجى التحقق من الشبكة والمحاولة لاحقاً"))
        } catch (e: NoCredentialException) {
            Log.w("AuthRepository", "Graceful NoCredentialException: No Google Account found on device", e)
            Result.failure(
                NoGoogleAccountException("لم يتم العثور على حساب جوجل مسجل على الجهاز. يمكنك الدخول ببريدك أو التجربة كزائر")
            )
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthRepository", "Google Sign-in cancelled by user")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.w("AuthRepository", "Credential Manager exception: ${e.message}", e)
            val isNoCred = e.message?.contains("No credentials", ignoreCase = true) == true ||
                    e.message?.contains("no account", ignoreCase = true) == true ||
                    e.type == "android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL"
            if (isNoCred) {
                Result.failure(
                    NoGoogleAccountException("لم يتم العثور على حساب جوجل مسجل على الجهاز. يمكنك الدخول ببريدك أو التجربة كزائر")
                )
            } else {
                Result.failure(
                    Exception("تعذر إكمال تسجيل الدخول بواسطة Google: ${e.localizedMessage ?: "يرجى المحاولة مجدداً أو الدخول كزائر"}")
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign-in unexpected exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val user = withTimeout(20_000L) {
                val result = auth.signInWithEmailAndPassword(email.trim().lowercase(), pass).await()
                result.user ?: throw IllegalStateException("بيانات المستخدم غير صالحة")
            }
            Result.success(user)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("AuthRepository", "Email sign-in timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالخادم، يرجى التحقق من الشبكة والمحاولة لاحقاً"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Email sign-in failed: ${e.message}", e)
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
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("AuthRepository", "Email sign-up timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالخادم، يرجى التحقق من الشبكة والمحاولة لاحقاً"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Email sign-up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            withTimeout(20_000L) {
                auth.sendPasswordResetEmail(email.trim().lowercase()).await()
            }
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("AuthRepository", "Password reset timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالخادم، يرجى التحقق من الشبكة والمحاولة لاحقاً"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Password reset failed: ${e.message}", e)
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
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("AuthRepository", "Password update timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالخادم، يرجى التحقق من الشبكة والمحاولة لاحقاً"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Password update failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
