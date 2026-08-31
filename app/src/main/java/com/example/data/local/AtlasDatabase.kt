package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AtlasPoiEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AtlasDatabase : RoomDatabase() {
    abstract fun atlasPoiDao(): AtlasPoiDao
}
