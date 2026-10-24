package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class ScopeApplication : Application() {

    companion object {
        private const val TAG = "ScopeApplication"

        lateinit var instance: ScopeApplication
            private set

        @Volatile
        var isFirebaseInitialized: Boolean = false
            private set

        @Volatile
        var initializationError: Throwable? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupGlobalExceptionHandler()
        initFirebaseSafely()
    }

    private fun initFirebaseSafely() {
        try {
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                isFirebaseInitialized = app != null
                Log.i(TAG, "FirebaseApp initialized explicitly: ${app?.name}")
            } else {
                isFirebaseInitialized = true
                Log.i(TAG, "FirebaseApp already initialized (${apps.size} app registered).")
            }
        } catch (e: Throwable) {
            isFirebaseInitialized = false
            initializationError = e
            Log.e(TAG, "CRITICAL HANDLED: Firebase initialization failed in ScopeApplication. App will run safely in fallback mode.", e)
        }
    }

    private fun setupGlobalExceptionHandler() {
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "FATAL EXCEPTION in thread [${thread.name}]: ${throwable.message}", throwable)
            originalHandler?.uncaughtException(thread, throwable)
        }
    }
}