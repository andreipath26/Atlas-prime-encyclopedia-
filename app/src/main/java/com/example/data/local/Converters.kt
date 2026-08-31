package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AtlasLayerType

class Converters {
    @TypeConverter
    fun fromLayerType(value: AtlasLayerType): String = value.name

    @TypeConverter
    fun toLayerType(value: String): AtlasLayerType = AtlasLayerType.valueOf(value)
}
