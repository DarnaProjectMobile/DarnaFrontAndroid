package com.sim.darna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _clientSecret = MutableStateFlow<String?>(null)
    val clientSecret: StateFlow<String?> = _clientSecret.asStateFlow()

    fun createPaymentIntent(
        amount: Int // montant en euros (le backend multiplie par 100 pour Stripe)
    ) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val secret = repository.createPaymentIntent(amount = amount)
                _clientSecret.value = secret
                _uiState.value = PaymentUiState.Success(secret)
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(
                    message = e.message ?: "Une erreur est survenue lors de la création du paiement"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = PaymentUiState.Idle
        _clientSecret.value = null
    }

    fun onPaymentSuccess() {
        _uiState.value = PaymentUiState.PaymentCompleted
    }

    fun onPaymentError(errorMessage: String) {
        _uiState.value = PaymentUiState.Error(message = errorMessage)
    }
}

