package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppVersionConfig(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val updateUrl: String = "https://github.com/Som3ael7amaky/SCOPE/releases/latest",
    val releaseNotes: String = "تحسينات شاملة واستقرار الأداء وإصلاحات عامة.",
    val forceUpdate: Boolean = false
)

class ScopeUpdateManager {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "ScopeUpdateManager"
        private const val GITHUB_VERSION_URL = "https://raw.githubusercontent.com/Som3ael7amaky/SCOPE/main/version.json"
        private const val DEFAULT_RELEASES_URL = "https://github.com/Som3ael7amaky/SCOPE/releases/latest"
    }

    suspend fun checkLatestVersion(): AppVersionConfig? = withContext(Dispatchers.IO) {
        // 1. Primary Check: Firestore Remote Config
        try {
            if (firestore != null) {
                val snapshot = firestore.collection("app_config")
                    .document("version_info")
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val config = snapshot.toObject(AppVersionConfig::class.java)
                    if (config != null) {
                        return@withContext config
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore version check failed, attempting GitHub version endpoint: ${e.message}")
        }

        // 2. Secondary Check: GitHub raw version.json
        try {
            val request = Request.Builder()
                .url(GITHUB_VERSION_URL)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string()
                if (!jsonStr.isNullOrBlank()) {
                    val json = JSONObject(jsonStr)
                    return@withContext AppVersionConfig(
                        latestVersionCode = json.optInt("versionCode", BuildConfig.VERSION_CODE),
                        latestVersionName = json.optString("versionName", BuildConfig.VERSION_NAME),
                        updateUrl = json.optString("downloadUrl", DEFAULT_RELEASES_URL),
                        releaseNotes = json.optString("releaseNotes", "تحديث جديد لنظام SCOPE."),
                        forceUpdate = json.optBoolean("forceUpdate", false)
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "GitHub raw version check skipped or failed: ${e.message}")
        }

        // 3. Fallback: current version with clean GitHub release link
        AppVersionConfig(
            latestVersionCode = BuildConfig.VERSION_CODE,
            latestVersionName = BuildConfig.VERSION_NAME,
            updateUrl = DEFAULT_RELEASES_URL,
            releaseNotes = "أنت تستخدم أحدث إصدار مثبت من SCOPE."
        )
    }

    fun isUpdateAvailable(latestCode: Int): Boolean {
        return latestCode > BuildConfig.VERSION_CODE
    }
}
