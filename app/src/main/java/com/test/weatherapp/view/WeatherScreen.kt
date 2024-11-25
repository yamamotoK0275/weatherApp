package com.test.weatherapp.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.test.weatherapp.R
import com.test.weatherapp.model.DataList
import com.test.weatherapp.model.WeatherData
import com.test.weatherapp.viewmodel.WeatherViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 天気画面
 *
 * @param viewModel
 * @param back トップバーの戻るボタン押下時処理
 */
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    back: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    WeatherScreen(
        uiState = uiState,
        back = back,
        load = {
            viewModel.load()
        },
        moveToIdle = {
            viewModel.moveToIdle()
        },
        retryConnection = {
            // リトライボタン押下時は、初期表示と同様の処理を行う
            viewModel.load()
        }
    )
}

/**
 * 天気画面
 *
 * ViewModelへの依存を回避するため関数、変数などを引数で受け取る形にする
 *
 * @param uiState
 * @param load
 * @param moveToIdle
 * @param retryConnection
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun WeatherScreen(
    uiState: WeatherViewModel.UiState,
    back: () -> Unit,
    load: () -> Unit,
    moveToIdle: () -> Unit,
    retryConnection: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.weather_title)) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when(uiState) {
            WeatherViewModel.UiState.Initial,
            WeatherViewModel.UiState.Loading,
            WeatherViewModel.UiState.LoadSuccess -> {
                // 読み込み中レイアウトを表示
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ){
                    CircularProgressIndicator()
                }
            }
            is WeatherViewModel.UiState.LoadError -> {
                // エラー画面を表示
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "通信に失敗しました。",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Button(
                            onClick = retryConnection
                        ) {
                            Text(text = "リトライ")
                        }
                    }
                }
            }
            is WeatherViewModel.UiState.Idle -> {
                // 天気情報をリスト表示
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.data.list) { data ->
                        WeatherCard(data = data, modifier = Modifier)
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        when(uiState) {
            WeatherViewModel.UiState.Initial -> {
                // 表示情報の読み込み
                load()
            }
            WeatherViewModel.UiState.Loading -> {
                // NOP
            }
            WeatherViewModel.UiState.LoadSuccess -> {
                // アイドル状態へ移行
                moveToIdle()
            }
            is WeatherViewModel.UiState.LoadError -> {
                // NOP
            }
            is WeatherViewModel.UiState.Idle -> {
                // NOP
            }
        }
    }
}

/**
 *  天気カード
 *
 *  @param data 1つ分の表示データ
 *  @param modifier
 */
@Composable
fun WeatherCard(data: DataList, modifier: Modifier) {
    val unix = data.dt
    val date = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix), ZoneId.of("Asia/Tokyo")))
    val temp = data.main.temp
    val icon = data.weather[0].icon

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF6cadee))
        ) {
            Text(
                text = date,
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp)
            )
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = "https://openweathermap.org/img/wn/$icon@2x.png"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .padding(4.dp)
                )
                Text(
                    text = "$temp℃",
                    fontSize = 48.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

/**
 * 正常時のプレビュー
 *
 * プレビューではAPIが叩け無いためリスト部分は空になる。
 * ローカルでデータを渡して仮表示などで確認するのが良いかとは思うが、
 * 天気アイコンもUrlから取得しているなどの処理もあるため今回は省略
 */
@Preview
@Composable
private fun Preview() {
    WeatherScreen(
        uiState = WeatherViewModel.UiState.Idle(WeatherData()),
        back = { },
        load = { },
        moveToIdle = { }
    ) {
    }
}

/**
 * エラー画面のプレビュー
 */
@Preview
@Composable
private fun PreviewError() {
    WeatherScreen(
        uiState = WeatherViewModel.UiState.LoadError(""),
        back = { },
        load = { },
        moveToIdle = { }
    ) {
    }
}


