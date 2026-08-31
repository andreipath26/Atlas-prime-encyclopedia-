package com.example.data.model

enum class AtlasLayerType {
    BOTANY, SCIENCE, HISTORY, GEOGRAPHY, CULTURE
}

data class AtlasPointOfInterest(
    val id: String,
    val title: String,
    val description: String,
    val layerType: AtlasLayerType,
    val latitude: Double,
    val longitude: Double
)
