package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AtlasPoiDao {
    @Query("SELECT * FROM poi_table")
    suspend fun getAllPoints(): List<AtlasPoiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<AtlasPoiEntity>)

    @Query("DELETE FROM poi_table WHERE layerType = :layerType")
    suspend fun deletePointsByLayer(layerType: String)
}
