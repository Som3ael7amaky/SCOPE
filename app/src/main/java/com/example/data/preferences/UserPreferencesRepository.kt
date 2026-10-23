package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val LAST_SEEN_VERSION_CODE = intPreferencesKey("last_seen_version_code")
    }

    val lastSeenVersionCode: Flow<Int> = dataStore.data.map { preferences ->
        preferences[LAST_SEEN_VERSION_CODE] ?: 0
    }

    suspend fun saveLastSeenVersionCode(versionCode: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_SEEN_VERSION_CODE] = versionCode
        }
    }
}
