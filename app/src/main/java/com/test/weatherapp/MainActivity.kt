package com.test.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.test.weatherapp.ui.theme.WeatherAppTheme
import com.test.weatherapp.view.HomeScreen
import com.test.weatherapp.view.WeatherScreen
import com.test.weatherapp.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                val navController = rememberNavController();
                MainNavHost(
                    navController = navController,
                    locationClient = fusedLocationClient,
                )
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
}

/**
 * MainNavHost
 *
 * @param navController
 */
@Composable
fun MainNavHost(
    navController: NavHostController,
    locationClient: FusedLocationProviderClient,
) {
    NavHost(navController = navController, startDestination = "/homeScreen") {
        // Home画面
        composable(route = "/homeScreen") {
            HomeScreen(
                navController = navController,
                locationClient = locationClient
            )
        }
        // 都市名選択して天気画面へ
        composable(
            route = "/weatherScreen/{cityName}",
            arguments = listOf(
                navArgument("cityName") {
                    type = NavType.StringType
                }
            )
        ) {
            val viewModel: WeatherViewModel = hiltViewModel()
            WeatherScreen(
                viewModel = viewModel,
                back = {
                    navController.popBackStack()
                }
            )
        }
        // 現在地を選択して天気画面へ
        composable(
            route = "/weatherScreen/{lat}&{lon}",
            arguments = listOf(
                navArgument("lat") {
                    type = NavType.FloatType
                },
                navArgument("lon") {
                    type = NavType.FloatType
                }
            )
        ){
            val viewModel: WeatherViewModel = hiltViewModel()
            WeatherScreen(
                viewModel = viewModel,
                back = {
                    navController.popBackStack()
                }
            )
        }
    }
}

