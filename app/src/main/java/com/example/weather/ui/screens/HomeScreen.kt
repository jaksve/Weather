package com.example.weather.ui.screens


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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
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
fun CloudIcon(cloudAreaFraction: Int) {
    if (cloudAreaFraction < 40) {
        Icon(
            imageVector = Icons.Outlined.WbSunny,
            contentDescription = "Sunny",
            tint = Color(0xFFFFB300)   // amber
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = "Cloudy",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun ForecastList(
    modifier: Modifier = Modifier,
    forecasts: List<Forecast>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(forecasts) { index, forecast ->
            ForecastRow(day = index + 1, forecast = forecast)
        }
    }
}

@Composable
fun ForecastRow(day: Int, forecast: Forecast) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                // ... day title, temp, wind texts
            }
            CloudIcon(cloudAreaFraction = forecast.cloudAreaFraction)
        }
        Column(modifier = Modifier.padding(12.dp)) {
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
                text = "Wind: ${forecast.windDirection}°  •  Cloud: ${forecast.cloudAreaFraction}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
    @Composable
    fun HomeScreen(
        vm: HomeViewModel
    ) {
        var lat by remember { mutableStateOf("") }
        var lon by remember { mutableStateOf("") }
        val sevenForecast by vm.forecast.collectAsState()
        Scaffold()
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                ForecastList(
                    forecasts = sevenForecast
                )
                //stockholm :  59.32944° N, 18.06861° E

                //haparanda 65°50′03″N, 24°07′01″Ö

            }
            OutlinedTextField(
                value = lon,
                onValueChange = { lon = it },
                label = { Text("Longitude") },
                placeholder = { Text("e.g. 18.077207") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Latitude") },
                placeholder = { Text("e.g. 59.33036") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val latVal = lat.toDoubleOrNull()
                    val lonVal = lon.toDoubleOrNull()
                    if (latVal != null && lonVal != null) {
                        vm.search(latVal, lonVal)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Search")
            }
            Spacer(Modifier.height(16.dp))
        
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Button(onClick = {
                    vm.search(59.32944, 18.06861)
                })
                {
                    Text(text = "Stockholm")
                }

                Button(onClick = {
                    vm.search(65.83417, 24.11694)
                })
                {
                    Text(text = "Haparanda")
                }
            }
        }
    }
