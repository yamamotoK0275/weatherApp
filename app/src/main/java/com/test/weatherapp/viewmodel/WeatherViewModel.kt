package com.test.weatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.weatherapp.model.WeatherData
import com.test.weatherapp.repository.WeatherRepository
import com.test.weatherapp.util.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject

/**
 * 天気画面ViewModel
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(){
    // 選択された都市
    private val cityName: String = savedStateHandle.get<String>("cityName") ?: ""
    // 現在地の緯度
    private val currentLocationLat: Float? = savedStateHandle.get<Float>("lat")
    // 現在地の経度
    private val currentLocationLon: Float? = savedStateHandle.get<Float>("lon")

    // 天気画面の状態一覧
    sealed interface UiState {
        // 初期化
        data object Initial : UiState
        // 読込中
        data object Loading : UiState
        // 読み込み成功
        data object LoadSuccess : UiState
        // 読み込み失敗
        data class LoadError(val cacheJson: String) : UiState
        // アイドル状態
        data class Idle(val data: WeatherData): UiState
    }

    private var _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 取得した天気情報
    private var weatherData: WeatherData = WeatherData()

    /**
     * 天気情報apiの読み込み
     */
    fun load() {
        // ローディングへ状態を遷移
        _uiState.value = UiState.Loading

        // 成功時処理
        val successProcess: (String) -> Unit = { result ->
            // 取得したJsonをローカルへ保存
            viewModelScope.launch(Dispatchers.IO) {
                weatherRepository.create(city = cityName, json = result)
            }

            // Jsonをキャスト
            weatherData = Json.decodeFromString<WeatherData>(result)
            // 読み込み成功へ状態を遷移
            _uiState.value = UiState.LoadSuccess
        }
        // 失敗時処理
        val failureProcess: (String) -> Unit = { _ ->
            // キャッシュされたデータを取得する
            viewModelScope.launch(Dispatchers.IO) {
                val json = weatherRepository.getWeatherByCity(city = cityName).json

                if (json.isEmpty()) {
                    // 読み込み失敗へ状態を遷移
                    _uiState.value = UiState.LoadError(json)
                } else {
                    // キャッシュデータをデコードしてアイドル状態にする
                    weatherData = Json.decodeFromString<WeatherData>(json)
                    _uiState.value = UiState.Idle(weatherData)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 都市名を選択した場合
                if(cityName.isNotEmpty()) {
                    // 当日のデータがキャッシュにあれば読み込み
                    val data = weatherRepository.getWeatherByCity(city = cityName)
                    if (data.json.isNotEmpty()) {
                        // 取得したデータの日時が今日であればキャッシュのデータを使用する
                        val dbDate = Date(TimeUtil.startTimeOfDay(data.createdAt))
                        val today = Date(TimeUtil.startTimeOfDay(System.currentTimeMillis()))
                        if(dbDate.compareTo(today) == 0) {
                            // Jsonをキャスト
                            weatherData = Json.decodeFromString<WeatherData>(data.json)
                            // 読み込み成功へ状態を遷移
                            _uiState.value = UiState.LoadSuccess
                            return@launch
                        }

                        // 当日のDBのデータではないため削除
                        weatherRepository.deleteWeatherByCity(data)
                    }

                    // 当日のキャッシュがないためAPIを呼び出し
                    weatherRepository.callOpenWeatherApiFromCityName(
                        cityName = cityName,
                        success = successProcess,
                        failure = failureProcess,
                    )
                } else if (currentLocationLat != null && currentLocationLon != null) { // 緯度経度から取得
                    weatherRepository.callOpenWeatherApiFromCoord(
                        lat = currentLocationLat.toDouble(),
                        lon = currentLocationLon.toDouble(),
                        success = successProcess,
                        failure = failureProcess,
                    )
                } else {
                    // 現在地が取得できなかった場合などの異常系に入ってくるため失敗時と同等の処理を行う
                    failureProcess("load Exception")
                }
            } catch (e: Exception) {
                Log.e("callApi", e.toString())
            }

        }
    }

    /**
     * アイドル状態へ移行
     */
    fun moveToIdle() {
        _uiState.value = UiState.Idle(weatherData)
    }

}