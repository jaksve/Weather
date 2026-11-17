package com.example.weather.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CloudIcon(cloudAreaFraction: Int) {
    if (cloudAreaFraction < 4) {
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