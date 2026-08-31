package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest
import com.example.ui.components.FloatingLayerBar
import com.example.ui.components.MapLibreClusteredView
import com.example.ui.components.PoiDetailPanel
import com.example.ui.theme.GlassBackground

@Composable
fun AtlasMapScreen(
    pointsOfInterest: List<AtlasPointOfInterest>,
    activeLayers: Set<AtlasLayerType>,
    selectedPoi: AtlasPointOfInterest?,
    onLayerToggle: (AtlasLayerType) -> Unit,
    onPoiSelected: (AtlasPointOfInterest) -> Unit,
    onPoiDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090D16))) {
        // --- MAP RENDER LAYER ---
        MapLibreClusteredView(
            activeLayers = activeLayers,
            pointsOfInterest = pointsOfInterest,
            onPoiSelected = onPoiSelected,
            modifier = Modifier.fillMaxSize()
        )
        
        // --- TOP FLOATING LAYER CONTROLS ---
        FloatingLayerBar(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp)
                .align(Alignment.TopCenter),
            activeLayers = activeLayers,
            onLayerToggle = onLayerToggle
        )

        // --- BOTTOM SHEET FOR SELECTED POI (Three UI Panel Style) ---
        if (selectedPoi != null) {
            PoiDetailPanel(
                poi = selectedPoi,
                onClose = onPoiDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            )
        }
    }
}
