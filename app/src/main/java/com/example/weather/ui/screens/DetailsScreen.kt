package com.example.weather.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.weather.ui.viewmodels.HomeViewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.data.DateTime
import com.example.weather.ui.viewmodels.Forecast



@Composable
fun ForecastList(
    modifier: Modifier = Modifier,
    forecasts: List<Forecast>
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(forecasts) { index, forecast ->
            ForecastRow(
                time = forecast.time,
                forecast = forecast)
        }
    }
}

@Composable
fun ForecastRow(
    time: DateTime,
    forecast: Forecast
) {
    Card(
        modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Time ${time.hour}",
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
fun DetailsScreen(
    vm: HomeViewModel,
    day :Int,
    onGoHome: ()->Unit
) {
    val detailedForecast by vm.detailedForecast.collectAsState()
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ForecastList(
                    forecasts = detailedForecast,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }

}
