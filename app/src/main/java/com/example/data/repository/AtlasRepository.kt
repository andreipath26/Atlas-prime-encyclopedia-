package com.example.data.repository

import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest

interface AtlasRepository {
    suspend fun fetchLayerPoints(layerType: AtlasLayerType): Result<List<AtlasPointOfInterest>>
}
