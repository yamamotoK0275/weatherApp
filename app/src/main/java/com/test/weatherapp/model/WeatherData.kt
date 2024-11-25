package com.test.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DataBase用データクラス
 *
 * @param id ID(自動生成される)
 * @param city 都市名
 * @param json Json
 * @param createdAt 保存日時(エピックタイム)
 */
data class WeatherDb(
    val id: Long,
    val city: String,
    val json: String,
    val createdAt: Long,
)

/**
 * OpenWeatherAPI#5dayキャスト用データクラス
 *
 * https://openweathermap.org/forecast5#5days
 */
@Serializable
data class WeatherData(
    val cod: String = "",
    val message: Int = 0,
    val cnt: Int = 0,
    val list: List<DataList> = listOf(),
    val city: City = City(),
)

@Serializable
data class DataList(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds? = null,
    val wind: Wind? = null,
    val visibility: Int,
    val pop: Double,
    val rain: Rain? = null,
    val snow: Snow? = null,
    val sys: Sys,
    val dt_txt: String,
)

@Serializable
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val sea_level: Int,
    val grnd_level: Int,
    val humidity: Int,
    val temp_kf: Double,
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

@Serializable
data class Clouds(
    val all: Int,
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double,
)

@Serializable
data class Rain(
    @SerialName("3h")
    val volume: Double
)

@Serializable
data class Snow(
    @SerialName("3h")
    val volume: Double
)

@Serializable
data class Sys(
    val pod: String,
)

@Serializable
data class City(
    val id: Long = 0,
    val name: String = "",
    val coord: Coord = Coord(),
    val country: String = "",
    val population: Int = 0,
    val timezone: Long = 0,
    val sunrise: Long = 0,
    val sunset: Long = 0,
)

@Serializable
data class Coord(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
)
