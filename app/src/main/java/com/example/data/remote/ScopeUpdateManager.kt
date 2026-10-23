package com.example.data.remote

import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppVersionConfig(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val updateUrl: String = "https://example.com/update",
    val forceUpdate: Boolean = false
)

class ScopeUpdateManager {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun checkLatestVersion(): AppVersionConfig? {
        return try {
            val snapshot = firestore.collection("app_config")
                .document("version_info")
                .get()
                .await()
            
            if (snapshot.exists()) {
                snapshot.toObject(AppVersionConfig::class.java)
            } else {
                // Return current as default if not found
                AppVersionConfig(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isUpdateAvailable(latestCode: Int): Boolean {
        return latestCode > BuildConfig.VERSION_CODE
    }
}
