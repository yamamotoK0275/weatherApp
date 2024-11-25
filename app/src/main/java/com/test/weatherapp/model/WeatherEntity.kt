package com.test.weatherapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    // テーブル名
    tableName = "weather"
)
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val city: String,
    val json: String,
    val createdAt: Long,
)