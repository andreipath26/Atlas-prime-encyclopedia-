package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.DependencyProvider
import com.example.ui.theme.NeonScience
import com.example.ui.viewmodel.AtlasViewModel
import com.example.ui.viewmodel.AtlasViewModelFactory

@Composable
fun AtlasMapRoute(
    viewModel: AtlasViewModel = viewModel(
        factory = AtlasViewModelFactory(DependencyProvider.repository)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        AtlasMapScreen(
            pointsOfInterest = uiState.pointsOfInterest,
            activeLayers = uiState.activeLayers,
            selectedPoi = uiState.selectedPoi,
            onLayerToggle = viewModel::toggleLayer,
            onPoiSelected = viewModel::selectPoi,
            onPoiDismiss = { viewModel.selectPoi(null) }
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = NeonScience
            )
        }
    }
}
