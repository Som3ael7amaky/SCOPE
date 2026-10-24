# ==============================================================================
# SCOPE - Comprehensive ProGuard & R8 Optimization Rules
# Ensures 100% Stability, Zero Stripping of Firebase, Auth, Firestore & Coroutines
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Attributes & Debugging Stack Traces
# ------------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# ------------------------------------------------------------------------------
# 2. Application Entry Points
# ------------------------------------------------------------------------------
-keep class com.example.ScopeApplication { *; }
-keep class com.example.MainActivity { *; }
-keep class com.example.widget.** { *; }

# ------------------------------------------------------------------------------
# 3. Kotlin & Coroutines Stability
# ------------------------------------------------------------------------------
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.android.** { *; }
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------
# 4. Firebase SDKs (Common, Auth, Firestore, App Check)
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**
-keepclassmembers class com.google.firebase.firestore.** { *; }

# Firebase App Check
-keep class com.google.firebase.appcheck.** { *; }
-dontwarn com.google.firebase.appcheck.**

# ------------------------------------------------------------------------------
# 5. Google Identity & AndroidX Credential Manager (Google Sign-In)
# ------------------------------------------------------------------------------
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**

# ------------------------------------------------------------------------------
# 6. SCOPE Data Models & Firestore DTO Serialization
# (Firestore reflection requires default constructors and fields)
# ------------------------------------------------------------------------------
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** {
    public <init>(...);
    public <fields>;
    public <methods>;
}

-keep class com.example.data.** { *; }

# ------------------------------------------------------------------------------
# 7. Jetpack Compose & AndroidX Lifecycle
# ------------------------------------------------------------------------------
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }

# ------------------------------------------------------------------------------
# 8. Moshi, Retrofit & OkHttp
# ------------------------------------------------------------------------------
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# ------------------------------------------------------------------------------
# 9. Room Database
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}
