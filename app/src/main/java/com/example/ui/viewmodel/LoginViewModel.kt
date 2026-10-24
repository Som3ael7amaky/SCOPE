package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG_AUTH = "AuthException"

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            Log.i(TAG_AUTH, "LoginViewModel: Starting signInWithGoogle flow")
            try {
                val result = authRepository.signInWithGoogle(context)
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    Log.i(TAG_AUTH, "LoginViewModel: Google Sign-In succeeded for user=${user.uid}, email=${user.email}")
                    checkAndElevateSuperAdmin(user)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    val ex = result.exceptionOrNull()
                    val errorMsg = ex?.message ?: "فشل تسجيل الدخول بواسطة Google"
                    Log.e(TAG_AUTH, "LoginViewModel: Google Sign-In failed: $errorMsg", ex)
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG_AUTH, "LoginViewModel: Unhandled exception in signInWithGoogle: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.message ?: "حدث خطأ غير متوقع أثناء تسجيل الدخول")
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            Log.i(TAG_AUTH, "LoginViewModel: Starting signInWithEmail flow for $email")
            try {
                val result = authRepository.signInWithEmail(email, pass)
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    Log.i(TAG_AUTH, "LoginViewModel: Email Sign-In succeeded for user=${user.uid}")
                    checkAndElevateSuperAdmin(user)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    val ex = result.exceptionOrNull()
                    val errorMsg = ex?.message ?: "فشل تسجيل الدخول، تحقق من البريد الإلكتروني وكلمة المرور"
                    Log.e(TAG_AUTH, "LoginViewModel: Email Sign-In failed: $errorMsg", ex)
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG_AUTH, "LoginViewModel: Unhandled exception in signInWithEmail: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.message ?: "حدث خطأ غير متوقع أثناء تسجيل الدخول")
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            Log.i(TAG_AUTH, "LoginViewModel: Starting signUpWithEmail flow for $email")
            try {
                val result = authRepository.signUpWithEmail(email, pass, displayName)
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    Log.i(TAG_AUTH, "LoginViewModel: Email Sign-Up succeeded for user=${user.uid}")
                    checkAndElevateSuperAdmin(user)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    val ex = result.exceptionOrNull()
                    val errorMsg = ex?.message ?: "فشل إنشاء الحساب، يرجى المحاولة لاحقاً"
                    Log.e(TAG_AUTH, "LoginViewModel: Email Sign-Up failed: $errorMsg", ex)
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG_AUTH, "LoginViewModel: Unhandled exception in signUpWithEmail: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.message ?: "حدث خطأ غير متوقع أثناء إنشاء الحساب")
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            Log.i(TAG_AUTH, "LoginViewModel: Starting continueAsGuest flow")
            try {
                val result = authRepository.signInAnonymously()
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    Log.i(TAG_AUTH, "LoginViewModel: Guest Sign-In succeeded for UID=${user.uid}")
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    val ex = result.exceptionOrNull()
                    val errorMsg = ex?.message ?: "فشل تسجيل الدخول كزائر"
                    Log.e(TAG_AUTH, "LoginViewModel: Guest Sign-In failed: $errorMsg", ex)
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG_AUTH, "LoginViewModel: Unhandled exception in continueAsGuest: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.message ?: "حدث خطأ غير متوقع أثناء الدخول كزائر")
            }
        }
    }

    suspend fun checkAndElevateSuperAdmin(user: FirebaseUser) {
        val email = user.email?.trim()?.lowercase() ?: ""
        if (email == "ismailhamaky@gmail.com") {
            Log.i(TAG_AUTH, "SUPER ADMIN MATCHED: $email. Injecting role = 'developer' in Firestore...")
            try {
                val existing = firestoreRepository.getUserProfileOnce(user.uid)
                val updatedProfile = (existing ?: UserProfile(uid = user.uid, email = email)).copy(
                    email = "ismailhamaky@gmail.com",
                    displayName = user.displayName?.ifBlank { "المطور إسماعيل 👑" } ?: "المطور إسماعيل 👑",
                    role = "developer",
                    isDeveloper = true,
                    isPro = true,
                    equippedTitle = "المطور الأسمى 👑",
                    sparks = 99999,
                    tokens = 9999,
                    level = 99
                )
                firestoreRepository.saveUserProfile(updatedProfile)
                Log.i(TAG_AUTH, "SUPER ADMIN SUCCESSFULLY INJECTED: role='developer', isDeveloper=true in Firestore!")
            } catch (e: Exception) {
                Log.e(TAG_AUTH, "Failed to inject Super Admin profile into Firestore: ${e.message}", e)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
