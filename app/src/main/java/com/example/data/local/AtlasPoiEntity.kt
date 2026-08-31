package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AtlasLayerType

@Entity(tableName = "poi_table")
data class AtlasPoiEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val layerType: AtlasLayerType,
    val latitude: Double,
    val longitude: Double
)
