package com.sim.darna.components

import androidx.activity.ComponentActivity
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

object PaymentSheetManager {
    private var paymentSheet: PaymentSheet? = null
    private var callback: ((PaymentSheetResult) -> Unit)? = null

    fun initialize(activity: ComponentActivity) {
        if (paymentSheet == null) {
            try {
                // Créer un callback temporaire qui sera remplacé plus tard
                paymentSheet = PaymentSheet(activity) { result ->
                    callback?.invoke(result)
                }
            } catch (e: IllegalStateException) {
                // Si l'Activity est déjà RESUMED, on ne peut pas créer le PaymentSheet
                android.util.Log.e("PaymentSheetManager", "Erreur lors de l'initialisation: ${e.message}", e)
            }
        }
    }

    fun setCallback(resultCallback: (PaymentSheetResult) -> Unit) {
        callback = resultCallback
    }

    fun presentPaymentIntent(
        clientSecret: String,
        merchantDisplayName: String = "Darna"
    ) {
        // Configuration personnalisée du PaymentSheet avec style moderne
        val configurationBuilder = PaymentSheet.Configuration.Builder(merchantDisplayName)
        
        // Personnalisation de l'apparence avec les couleurs modernes (style payment interface)
        try {
            // Créer les couleurs personnalisées avec les bons noms de paramètres
            val colors = PaymentSheet.Colors(
                primary = android.graphics.Color.parseColor("#0066FF"), // Bleu primaire moderne
                surface = android.graphics.Color.parseColor("#FFFFFF"), // Fond blanc (surface)
                component = android.graphics.Color.parseColor("#FFFFFF"), // Fond des composants blanc
                componentBorder = android.graphics.Color.parseColor("#E0E0E0"), // Bordure grise claire
                componentDivider = android.graphics.Color.parseColor("#E0E0E0"), // Diviseur gris clair
                onComponent = android.graphics.Color.parseColor("#1A1A1A"), // Texte sur composants (sombre)
                onSurface = android.graphics.Color.parseColor("#1A1A1A"), // Texte principal sombre
                subtitle = android.graphics.Color.parseColor("#757575"), // Texte secondaire gris
                placeholderText = android.graphics.Color.parseColor("#B0BEC5"), // Texte placeholder
                appBarIcon = android.graphics.Color.parseColor("#0066FF"), // Icônes bleues
                error = android.graphics.Color.parseColor("#F44336") // Couleur d'erreur rouge
            )
            
            // Créer la forme du bouton primaire
            val primaryButtonShape = PaymentSheet.PrimaryButtonShape(
                cornerRadiusDp = 12.0f // Coins arrondis modernes
            )
            
            // Créer le bouton primaire - seul shape est nécessaire
            val primaryButton = PaymentSheet.PrimaryButton(
                shape = primaryButtonShape
            )
            
            // Créer les formes générales
            val shapes = PaymentSheet.Shapes(
                cornerRadiusDp = 12.0f, // Coins arrondis pour tous les composants
                borderStrokeWidthDp = 1.0f // Largeur de bordure
            )
            
            // Créer l'apparence personnalisée avec les paramètres nommés
            val appearance = PaymentSheet.Appearance(
                colorsLight = colors,
                shapes = shapes,
                primaryButton = primaryButton
            )
            
            configurationBuilder.appearance(appearance)
        } catch (e: Exception) {
            // Si l'API n'est pas disponible dans cette version, utiliser la configuration de base
            android.util.Log.w("PaymentSheetManager", "Impossible d'appliquer l'apparence personnalisée: ${e.message}. Utilisation de la configuration par défaut.")
        }
        
        val configuration = configurationBuilder.build()

        paymentSheet?.presentWithPaymentIntent(
            clientSecret,
            configuration
        ) ?: throw IllegalStateException("PaymentSheet n'est pas initialisé. Assurez-vous que MainActivity.onCreate a été appelé.")
    }

    fun clear() {
        paymentSheet = null
        callback = null
    }
}


