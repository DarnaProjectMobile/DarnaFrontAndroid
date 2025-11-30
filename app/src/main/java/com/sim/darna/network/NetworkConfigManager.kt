package com.sim.darna.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

private val Context.networkConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "network_config_preferences")

object NetworkConfigManager {
    private val BACKEND_URL_KEY = stringPreferencesKey("backend_url")

    /**
     * Sauvegarde l'URL du backend dans DataStore
     */
    fun saveBackendUrl(context: Context, url: String) {
        runBlocking {
            context.networkConfigDataStore.edit { preferences ->
                preferences[BACKEND_URL_KEY] = url
            }
        }
        // Clear cache in NetworkConfig to force refresh
        NetworkConfig.clearCache()
    }

    /**
     * Récupère l'URL du backend depuis DataStore
     */
    suspend fun getBackendUrl(context: Context): String? {
        return try {
            val preferences = context.networkConfigDataStore.data.firstOrNull()
            preferences?.get(BACKEND_URL_KEY)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Version synchrone pour compatibilité avec le code existant
     */
    fun getBackendUrlSync(context: Context): String? {
        return runBlocking {
            getBackendUrl(context)
        }
    }
}
