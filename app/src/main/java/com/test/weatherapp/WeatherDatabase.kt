package com.test.weatherapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.test.weatherapp.dao.WeatherDao
import com.test.weatherapp.model.WeatherEntity

@Database(
    entities = [
        WeatherEntity::class
    ],
    version = 1,
    exportSchema = true,
)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}