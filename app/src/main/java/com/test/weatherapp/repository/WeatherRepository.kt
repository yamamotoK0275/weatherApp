package com.test.weatherapp.repository

import com.test.weatherapp.model.WeatherDb

interface WeatherRepository {
    suspend fun create(city: String, json: String): WeatherDb
    suspend fun getWeatherByCity(city: String): WeatherDb
    suspend fun deleteWeatherByCity(weather: WeatherDb)
    suspend fun callOpenWeatherApiFromCityName(cityName: String, success: (String) -> Unit, failure: (String) -> Unit, )
    suspend fun callOpenWeatherApiFromCoord(lat: Double, lon: Double, success: (String) -> Unit, failure: (String) -> Unit, )
}