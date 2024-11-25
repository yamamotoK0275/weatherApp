package com.test.weatherapp.repository

import android.util.Log
import com.test.weatherapp.dao.WeatherDao
import com.test.weatherapp.model.WeatherDb
import com.test.weatherapp.model.WeatherEntity
import kotlinx.coroutines.flow.first
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * 天気画面リポジトリ
 */
class LocalWeatherRepository @Inject constructor(
    private val weatherDao: WeatherDao,
): WeatherRepository {

    /**
     * DBへ保存
     *
     * @param city
     * @param json
     */
    override suspend fun create(city: String, json: String): WeatherDb {
        val weather = WeatherEntity(
            id = 0,
            city = city,
            json = json,
            createdAt = System.currentTimeMillis(),
        )
        val id = weatherDao.create(weather = weather)
        return WeatherDb(
            id = id,
            city = city,
            json = json,
            createdAt = weather.createdAt
        )
    }

    /**
     * 都市名からDBの天気情報を取得
     *
     * @param city
     */
    override suspend fun getWeatherByCity(city: String): WeatherDb {
        val isExist = weatherDao.checkByCity(cityName = city) != 0
        return if(isExist) {
            val weatherEntity = weatherDao.findByCity(cityName = city).first().first()
            WeatherDb(
                id = weatherEntity.id,
                city = weatherEntity.city,
                json = weatherEntity.json,
                createdAt = weatherEntity.createdAt
            )
        } else {
            WeatherDb(
                id = 0,
                city = "",
                json = "",
                createdAt = System.currentTimeMillis()
            )
        }
    }

    /**
     * データ削除
     *
     * @param weather 削除するデータ
     */
    override suspend fun deleteWeatherByCity(weather: WeatherDb) {
        val entity = WeatherEntity(
            id = weather.id,
            city = weather.city,
            json = weather.json,
            createdAt = weather.createdAt,
        )
        weatherDao.deleteByCity(entity)
    }

    /**
     * 都市名からAPIを呼び出す
     *
     * @param cityName 都市名
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    override suspend fun callOpenWeatherApiFromCityName(
        cityName: String,
        success: (String) -> Unit,
        failure: (String) -> Unit,
    ) {
        callApi(
            cityName = cityName,
            lat = 0.0,
            lon = 0.0,
            success = success,
            failure = failure,
        )
    }

    /**
     * 緯度経度からAPIを呼び出す
     *
     * @param lat 緯度
     * @param lon 経度
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    override suspend fun callOpenWeatherApiFromCoord(
        lat: Double,
        lon: Double,
        success: (String) -> Unit,
        failure: (String) -> Unit,
    ) {
        callApi(
            cityName = "",
            lat = lat,
            lon = lon,
            success = success,
            failure = failure,
        )
    }

    /**
     * 天気API
     *
     * 都市名があれば都市名からリクエストし空文字であれば緯度経度として判断してリクエスト
     *
     * @param cityName 都市名
     * @param lat 緯度
     * @param lon 経度
     * @param success 成功時処理
     * @param failure　失敗時処理
     */
    private fun callApi(
        cityName: String,
        lat: Double,
        lon: Double,
        success: (String) -> Unit,
        failure: (String) -> Unit
    ) {
        val urlPrefix = "https://api.openweathermap.org/data/2.5/forecast?"
        val urlSuffix = "&appid=d2bf2aed4cb053135256b082b798eea5&units=metric&lang=ja"
        val url = if (cityName.isNotEmpty()) { // 都市名
            urlPrefix + "q=$cityName" + urlSuffix
        } else { // 都市名がなければ緯度経度として判断
            urlPrefix + "lat=$lat&lon=$lon" + urlSuffix
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OkHttpConnection", "Request Failed: ${e.message}")
                failure(e.message?: "")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful) {
                        Log.e("OkHttpConnection", "Unexpected response code: ${response.code}")
                        failure("Unexpected response: ${response.code}")
                    } else {
                        val responseBody = response.body?.string()
                        Log.d("OkHttpConnection", "Response: $responseBody")
                        success("$responseBody")
                    }
                }
            }
        })
    }
}