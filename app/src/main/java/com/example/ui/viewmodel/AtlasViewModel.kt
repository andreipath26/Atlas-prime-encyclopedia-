package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest
import com.example.data.repository.AtlasRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
// import javax.inject.Inject

data class AtlasUiState(
    val pointsOfInterest: List<AtlasPointOfInterest> = emptyList(),
    val activeLayers: Set<AtlasLayerType> = setOf(
        AtlasLayerType.HISTORY,
        AtlasLayerType.BOTANY,
        AtlasLayerType.SCIENCE
    ),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedPoi: AtlasPointOfInterest? = null
)

// @HiltViewModel
class AtlasViewModel /* @Inject constructor */ (
    private val repository: AtlasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtlasUiState())
    val uiState: StateFlow<AtlasUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Fetch active layers concurrently
            val deferreds = AtlasLayerType.entries.map { layer ->
                async { repository.fetchLayerPoints(layer) }
            }

            val results = deferreds.awaitAll()
            val allPoints = mutableListOf<AtlasPointOfInterest>()
            var errorMsg: String? = null

            results.forEach { result ->
                result.onSuccess { points -> allPoints.addAll(points) }
                    .onFailure { error -> errorMsg = error.localizedMessage }
            }

            _uiState.update {
                it.copy(
                    pointsOfInterest = allPoints,
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun toggleLayer(layer: AtlasLayerType) {
        _uiState.update { state ->
            val updatedLayers = if (state.activeLayers.contains(layer)) {
                state.activeLayers - layer
            } else {
                state.activeLayers + layer
            }
            state.copy(activeLayers = updatedLayers)
        }
    }

    fun selectPoi(poi: AtlasPointOfInterest?) {
        _uiState.update { it.copy(selectedPoi = poi) }
    }
}
