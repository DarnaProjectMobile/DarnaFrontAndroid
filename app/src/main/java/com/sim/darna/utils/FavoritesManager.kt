package com.sim.darna.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_FAVORITES = "favorite_property_ids"
    
    private var _favoritePropertyIds by mutableStateOf<Set<String>>(emptySet())
    val favoritePropertyIds: Set<String> get() = _favoritePropertyIds
    
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        _favoritePropertyIds = favorites
    }
    
    fun isFavorite(propertyId: String): Boolean {
        return _favoritePropertyIds.contains(propertyId)
    }
    
    fun toggleFavorite(context: Context, propertyId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentFavorites = _favoritePropertyIds.toMutableSet()
        
        if (currentFavorites.contains(propertyId)) {
            currentFavorites.remove(propertyId)
        } else {
            currentFavorites.add(propertyId)
        }
        
        _favoritePropertyIds = currentFavorites
        prefs.edit().putStringSet(KEY_FAVORITES, currentFavorites).apply()
    }
}

