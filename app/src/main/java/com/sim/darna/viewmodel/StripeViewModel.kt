package com.sim.darna.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.repository.StripeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StripePaymentState(
    val isLoading: Boolean = false,
    val paymentUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class StripeViewModel @Inject constructor(
    private val repository: StripeRepository
) : ViewModel() {
    
    private val _paymentState = MutableStateFlow(StripePaymentState())
    val paymentState: StateFlow<StripePaymentState> = _paymentState
    
    fun createPaymentIntent(context: Context, amount: Double, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _paymentState.value = StripePaymentState(isLoading = true)
            try {
                val response = repository.createPaymentIntent(context, amount)
                Log.d("StripeViewModel", "Response code: ${response.code()}")
                Log.d("StripeViewModel", "Response body: ${response.body()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val clientSecret = response.body()!!.clientSecret
                    if (clientSecret != null) {
                        _paymentState.value = StripePaymentState(paymentUrl = clientSecret)
                        onResult(true, clientSecret)
                    } else {
                        val error = "ClientSecret est null dans la réponse"
                        Log.e("StripeViewModel", error)
                        _paymentState.value = StripePaymentState(error = error)
                        onResult(false, null)
                    }
                } else {
                    val errorBody = try {
                        response.errorBody()?.string() ?: "Erreur ${response.code()}"
                    } catch (e: Exception) {
                        "Erreur ${response.code()}"
                    }
                    val error = "Erreur lors de la création du paiement: $errorBody"
                    Log.e("StripeViewModel", error)
                    _paymentState.value = StripePaymentState(error = error)
                    onResult(false, null)
                }
            } catch (e: Exception) {
                val error = "Exception: ${e.message}"
                Log.e("StripeViewModel", error, e)
                _paymentState.value = StripePaymentState(error = error)
                onResult(false, null)
            }
        }
    }
}

