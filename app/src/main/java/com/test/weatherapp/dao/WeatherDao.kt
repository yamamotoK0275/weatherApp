package com.test.weatherapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.test.weatherapp.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert
    suspend fun create(weather: WeatherEntity): Long

    @Query("SELECT * FROM weather WHERE city == :cityName")
    fun findByCity(cityName: String): Flow<List<WeatherEntity>>

    @Query("SELECT Count(*) FROM weather WHERE city == :cityName")
    fun checkByCity(cityName: String): Int

    @Delete
    fun deleteByCity(weather: WeatherEntity)
}