package com.sim.darna.payment

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
        paymentSheet?.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(merchantDisplayName = merchantDisplayName)
        ) ?: throw IllegalStateException("PaymentSheet n'est pas initialisé. Assurez-vous que MainActivity.onCreate a été appelé.")
    }

    fun clear() {
        paymentSheet = null
        callback = null
    }
}

