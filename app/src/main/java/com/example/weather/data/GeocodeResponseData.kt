package com.example.weather.data

data class GeocodeResponseData(
    val response : GeocodeApiResponse,
    val lon : Double = response.lon,
    val lat : Double = response.lat

)
