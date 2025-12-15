package com.sim.darna.notifications

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class StoredNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    // nullable to stay compatible with older stored JSON
    val data: Map<String, String>? = null,
) {
    fun formattedDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}

object NotificationStore {

    private const val PREFS_NAME = "APP_PREFS"
    private const val KEY_NOTIFICATIONS = "stored_notifications"
    private const val MAX_NOTIFICATIONS = 50

    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<StoredNotification>>() {}.type

    fun getNotifications(context: Context): List<StoredNotification> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<MutableList<StoredNotification>>(json, listType).toList()
        }.getOrElse { emptyList() }
    }

    fun saveNotification(
        context: Context,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = getNotifications(context).toMutableList()
        list.add(0, StoredNotification(title = title, body = body, data = data))
        if (list.size > MAX_NOTIFICATIONS) {
            while (list.size > MAX_NOTIFICATIONS) {
                list.removeAt(list.lastIndex)
            }
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, gson.toJson(list)).apply()
    }

    fun clearNotifications(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }

    fun removeNotification(context: Context, notificationId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = getNotifications(context).toMutableList()
        val updated = list.filterNot { it.id == notificationId }
        prefs.edit().putString(KEY_NOTIFICATIONS, gson.toJson(updated)).apply()
    }
}

