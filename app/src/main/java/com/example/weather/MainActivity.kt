package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.ui.screens.DetailsScreen
import com.example.weather.ui.screens.HomeScreen
import com.example.weather.ui.screens.SettingsScreen
import com.example.weather.ui.theme.WeatherTheme
import com.example.weather.ui.viewmodels.HomeVM
import com.example.weather.ui.viewmodels.SettingsVM

class MainActivity : ComponentActivity(

) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherTheme {
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp()
{
    var currentScreen by remember { mutableStateOf("home") }
    var currentDay by remember { mutableStateOf(0) }
    val homeViewModel: HomeVM = viewModel(
        factory = HomeVM.Factory
    )
   when (currentScreen) {
       "home"-> {

               HomeScreen(vm = homeViewModel,
               onGetWeather = { day-> homeViewModel.getWeatherDetails(day)
                   currentDay = day
               currentScreen = "details"},
                   onGetSettings = {currentScreen = "settings"})
       }
       "details"-> {
           homeViewModel.loadFavourites()
           DetailsScreen(vm = homeViewModel,currentDay,onGoHome = {currentScreen = "home"})
       }
       "settings"->{
           val settingsViewModel: SettingsVM = viewModel(factory = SettingsVM.Factory)
           SettingsScreen(settingsViewModel,
               onGetHome = {currentScreen = "home"})
       }
   }
}