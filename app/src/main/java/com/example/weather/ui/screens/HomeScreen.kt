package com.example.weather.ui.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.weather.ui.viewmodels.Forecast
import com.example.weather.ui.viewmodels.HomeViewModel

//Homescreen with a search function

@Composable
fun ForecastList(
    modifier: Modifier = Modifier,
    forecasts: List<Forecast>,
    onGetWeather:(day: Int)->Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(forecasts) { index, forecast ->
            ForecastRow(
                day = index + 1,
                forecast = forecast,
                onGetWeather = onGetWeather)
        }
    }
}

@Composable
fun ForecastRow(
    day: Int,
    forecast: Forecast,
    onGetWeather: (day: Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable {onGetWeather(forecast.time.day)}) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Day $day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Temp: ${String.format("%.1f", forecast.temp)}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Wind: ${forecast.windDirection}°  •  Cloud: ${(forecast.cloudAreaFraction/8)*100}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CloudIcon(cloudAreaFraction = forecast.cloudAreaFraction)
        }
    }
}

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onGetWeather:(day: Int)->Unit,
    onGetSettings: ()->Unit
) {
    //var lat by remember { mutableStateOf("") }
    //var lon by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    val sevenForecast by vm.forecast.collectAsState()
    val favourites by vm.favourites.collectAsState()


    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ForecastList(
                forecasts = sevenForecast,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onGetWeather = onGetWeather
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Place") },
                        placeholder = { Text("London") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        if (city != "") {
                            vm.search(city)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Search")
                }
                LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favourites, key = { it }) { favourite ->
                            Button(onClick = { vm.search(favourite) }) {
                                Text(text = favourite)
                            }
                        }
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Button(onClick = { onGetSettings() }) {
                        Text(text = "Settings")
                    }
                }
            }
        }
    }
}
