package com.test.weatherapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.test.weatherapp.R
import com.test.weatherapp.WeatherApplication

/**
 * HOME画面
 *
 * @param navController
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavHostController,
    locationClient: FusedLocationProviderClient,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.home_title)) },
            )
        }
    ) { innerPadding ->
        CityList(
            innerPadding = innerPadding,
            modifier = Modifier,
            navController,
            locationClient = locationClient,
        )
    }
}

/**
 * 都市一覧用データクラス
 *
 * @param cityName
 * @param cityQuery
 */
data class City(val cityName: String, val cityQuery: String)

/**
 * 都市リスト
 *
 * @param innerPadding
 * @param modifier
 * @param navController
 */
@SuppressLint("MissingPermission")
@Composable
fun CityList(
    innerPadding: PaddingValues,
    modifier: Modifier,
    navController: NavHostController,
    locationClient: FusedLocationProviderClient,
) {
    // 都市一覧
    val cityList = listOf(
        City("東京", "Tokyo"),
        City("兵庫", "Kobe"),
        City("大分", "Ōita"),
        City("北海道", "Hokkaido"),
    )

    // リストレイアウト
    LazyColumn(
        contentPadding = innerPadding,
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(cityList) { city ->
            ListCard(
                title = city.cityName,
                listClick = {
                    navController.navigate("/weatherScreen/${city.cityQuery}")
                },
                modifier = modifier
            )
        }
        item {
            val request = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if(isGranted) {
                    locationClient.lastLocation.addOnSuccessListener { location ->
                        Log.d("location", "latitude: ${location.latitude}")
                        Log.d("location", "longitude: ${location.longitude}")
                        navController.navigate("/weatherScreen/${location.latitude}&${location.longitude}")
                    }
                }
            }

            ListCard(
                title = "現在地",
                listClick = {
                    request.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = modifier)
        }
    }
}

/**
 * リストに表示するカードレイアウト
 *
 * @param title リストのタイトル
 * @param listClick クリック時処理
 * @param modifier
 */
@Composable
fun ListCard(
    title: String,
    listClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        color = Color(0xFF6cadee)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .clickable {
                    listClick()
                }
                .padding(24.dp)
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
            )
        }
    }
}

/**
 * ホーム画面プレビュー
 */
@Preview
@Composable
private fun PreviewList() {
    HomeScreen(
        navController = rememberNavController(),
        locationClient = LocationServices.getFusedLocationProviderClient(WeatherApplication.applicationContext())
    )
}