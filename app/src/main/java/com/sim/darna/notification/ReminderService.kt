package com.sim.darna.notification

import java.text.SimpleDateFormat
import java.util.*

/**
 * Service pour calculer les dates de rappels pour les visites
 * Crée des rappels : 1h avant, 2h avant, 1 jour avant, 2 jours avant
 */
object ReminderService {
    
    /**
     * Calcule toutes les dates de rappels pour une visite
     * @param visiteDateTime ISO date string de la visite (format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @return Liste des dates de rappels avec leur type
     */
    fun calculateReminders(visiteDateTime: String): List<ReminderInfo> {
        val reminders = mutableListOf<ReminderInfo>()
        
        try {
            // Parser la date de visite
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val visiteDate = parser.parse(visiteDateTime) ?: return emptyList()
            
            val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val visiteCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = visiteDate
            }
            
            // Calculer la différence en millisecondes
            val diffMillis = visiteCalendar.timeInMillis - now.timeInMillis
            val diffHours = diffMillis / (1000 * 60 * 60.0)
            val diffDays = diffMillis / (1000 * 60 * 60 * 24.0)
            
            // Si la visite est dans le passé, ne pas créer de rappels
            if (diffMillis <= 0) return emptyList()
            
            // Rappel 2 heures avant (ou maintenant si visite dans moins de 2h mais plus de 1h)
            if (diffHours >= 1 && diffHours < 2) {
                // Visite dans 1-2h : créer rappel maintenant
                reminders.add(
                    ReminderInfo(
                        type = "visite_reminder_2h",
                        scheduledFor = formatIsoDate(now.time),
                        title = "Rappel visite dans ${diffHours.toInt()}h",
                        message = "Vous avez une visite dans ${diffHours.toInt()} heure${if (diffHours.toInt() > 1) "s" else ""}"
                    )
                )
            } else if (diffHours >= 2) {
                // Visite dans plus de 2h : créer rappel 2h avant
                val reminder2h = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    time = visiteDate
                    add(Calendar.HOUR_OF_DAY, -2)
                }
                if (reminder2h.timeInMillis > now.timeInMillis) {
                    reminders.add(
                        ReminderInfo(
                            type = "visite_reminder_2h",
                            scheduledFor = formatIsoDate(reminder2h.time),
                            title = "Rappel visite dans 2h",
                            message = "Vous avez une visite dans 2 heures"
                        )
                    )
                }
            }
            
            // Rappel 1 heure avant (ou maintenant si visite dans moins de 1h)
            if (diffHours >= 0.5 && diffHours < 1) {
                // Visite dans moins de 1h : créer rappel maintenant
                val minutes = (diffHours * 60).toInt()
                reminders.add(
                    ReminderInfo(
                        type = "visite_reminder_1h",
                        scheduledFor = formatIsoDate(now.time),
                        title = "Rappel visite dans ${minutes}min",
                        message = "Vous avez une visite dans $minutes minute${if (minutes > 1) "s" else ""}"
                    )
                )
            } else if (diffHours >= 1) {
                // Visite dans plus de 1h : créer rappel 1h avant
                val reminder1h = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    time = visiteDate
                    add(Calendar.HOUR_OF_DAY, -1)
                }
                if (reminder1h.timeInMillis > now.timeInMillis) {
                    reminders.add(
                        ReminderInfo(
                            type = "visite_reminder_1h",
                            scheduledFor = formatIsoDate(reminder1h.time),
                            title = "Rappel visite dans 1h",
                            message = "Vous avez une visite dans 1 heure"
                        )
                    )
                }
            }
            
            // Rappel 2 jours avant (à 9h du matin)
            val reminder2d = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = visiteDate
                add(Calendar.DAY_OF_MONTH, -2)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (reminder2d.timeInMillis > System.currentTimeMillis()) {
                reminders.add(
                    ReminderInfo(
                        type = "visite_reminder_2d",
                        scheduledFor = formatIsoDate(reminder2d.time),
                        title = "Rappel visite dans 2 jours",
                        message = "Vous avez une visite dans 2 jours"
                    )
                )
            }
            
            // Rappel 1 jour avant (à 9h du matin)
            val reminder1d = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = visiteDate
                add(Calendar.DAY_OF_MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (reminder1d.timeInMillis > System.currentTimeMillis()) {
                reminders.add(
                    ReminderInfo(
                        type = "visite_reminder_1d",
                        scheduledFor = formatIsoDate(reminder1d.time),
                        title = "Rappel visite demain",
                        message = "Vous avez une visite demain"
                    )
                )
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ReminderService", "Erreur lors du calcul des rappels", e)
        }
        
        return reminders
    }
    
    /**
     * Calcule les rappels pour un colocataire (rappel de rendez-vous visite)
     * Même logique que pour le client, mais avec titre adapté
     */
    fun calculateCollocatorReminders(visiteDateTime: String, clientUsername: String, logementTitle: String): List<ReminderInfo> {
        // Utiliser la même logique de calcul que pour le client
        val reminders = calculateReminders(visiteDateTime)
        
        // Adapter uniquement le titre pour indiquer "rendez-vous visite"
        return reminders.map { reminder ->
            reminder.copy(
                type = reminder.type, // Garder le même type (visite_reminder_1h, etc.)
                title = reminder.title.replace("visite", "rendez-vous visite"),
                message = reminder.message.replace("visite", "rendez-vous visite")
            )
        }
    }
    
    private fun formatIsoDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(date)
    }
}

data class ReminderInfo(
    val type: String,
    val scheduledFor: String,
    val title: String,
    val message: String
)

