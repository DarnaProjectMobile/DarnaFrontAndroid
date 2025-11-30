package com.sim.darna.viewmodel

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val clientSecret: String) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
    object PaymentCompleted : PaymentUiState()
}
