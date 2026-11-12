package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.ui.screens.HomeScreen
import com.example.weather.ui.theme.WeatherTheme
import com.example.weather.ui.viewmodels.HomeVM

class MainActivity : ComponentActivity(

) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeViewModel: HomeVM = viewModel(
                factory = HomeVM.Factory
            )
            WeatherTheme {
                HomeScreen(vm = homeViewModel)
            }
        }
    }
}

@Composable
fun test()
{
    val homeViewModel: HomeVM = viewModel(
        factory = HomeVM.Factory
    )
}