package com.sim.darna.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.model.Property
import com.sim.darna.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class PropertyUiState(
    val properties: List<Property> = emptyList(),
    val filteredProperties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchText: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val ownershipFilter: OwnershipFilter = OwnershipFilter.ALL
)

enum class OwnershipFilter {
    ALL, MINE, NOT_MINE
}

class PropertyViewModel(private val context: Context) : ViewModel() {
    
    private val repository = PropertyRepository(context)
    
    private val _uiState = MutableStateFlow(PropertyUiState())
    val uiState: StateFlow<PropertyUiState> = _uiState.asStateFlow()
    
    private var currentUserId: String? = null
    
    fun init(userId: String?) {
        currentUserId = userId
    }
    
    fun loadProperties() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        repository.getAllProperties().enqueue(object : Callback<List<Property>> {
            override fun onResponse(call: Call<List<Property>>, response: Response<List<Property>>) {
                if (response.isSuccessful && response.body() != null) {
                    val properties = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        properties = properties,
                        isLoading = false
                    )
                    applyFilters()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Impossible de charger les annonces."
                    )
                }
            }
            
            override fun onFailure(call: Call<List<Property>>, t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur de connexion: ${t.message}"
                )
            }
        })
    }
    
    fun setSearchText(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text)
        applyFilters()
    }
    
    fun setPriceFilter(min: Double?, max: Double?) {
        _uiState.value = _uiState.value.copy(minPrice = min, maxPrice = max)
        applyFilters()
    }
    
    fun setOwnershipFilter(filter: OwnershipFilter) {
        _uiState.value = _uiState.value.copy(ownershipFilter = filter)
        applyFilters()
    }
    
    fun toggleOwnershipFilter(filter: OwnershipFilter) {
        val current = _uiState.value.ownershipFilter
        val newFilter = if (current == filter) OwnershipFilter.ALL else filter
        setOwnershipFilter(newFilter)
    }
    
    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.properties.filter { property ->
            // Search filter
            val matchesSearch = state.searchText.isEmpty() ||
                    property.title.lowercase().contains(state.searchText.lowercase())
            
            // Price filters
            val matchesMinPrice = state.minPrice == null || property.price >= state.minPrice!!
            val matchesMaxPrice = state.maxPrice == null || property.price <= state.maxPrice!!
            
            // Ownership filter
            val matchesOwnership = when (state.ownershipFilter) {
                OwnershipFilter.ALL -> true
                OwnershipFilter.MINE -> property.user == currentUserId
                OwnershipFilter.NOT_MINE -> property.user != currentUserId
            }
            
            matchesSearch && matchesMinPrice && matchesMaxPrice && matchesOwnership
        }
        
        _uiState.value = state.copy(filteredProperties = filtered)
    }
    
    fun deleteProperty(property: Property, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.deleteProperty(property.id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    val updatedList = _uiState.value.properties.filter { it.id != property.id }
                    _uiState.value = _uiState.value.copy(properties = updatedList)
                    applyFilters()
                    onSuccess()
                } else {
                    onError("Suppression impossible")
                }
            }
            
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                onError("Erreur: ${t.message}")
            }
        })
    }
    
    fun refreshProperties() {
        loadProperties()
    }
}

