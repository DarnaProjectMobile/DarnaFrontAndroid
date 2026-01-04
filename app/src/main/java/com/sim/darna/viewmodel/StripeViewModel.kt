package com.sim.darna.viewmodel

import android.content.Context
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
                if (response.isSuccessful && response.body() != null) {
                    val clientSecret = response.body()!!.clientSecret
                    _paymentState.value = StripePaymentState(paymentUrl = clientSecret)
                    onResult(true, clientSecret)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Payment error (${response.code()}): $errorBody"
                    android.util.Log.e("StripeViewModel", errorMsg)
                    _paymentState.value = StripePaymentState(error = errorMsg)
                    onResult(false, null)
                }
            } catch (e: Exception) {
                android.util.Log.e("StripeViewModel", "Payment exception: ${e.message}", e)
                _paymentState.value = StripePaymentState(error = e.message)
                onResult(false, null)
            }
        }
    }
}

