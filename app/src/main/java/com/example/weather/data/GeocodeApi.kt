package com.example.weather.data

// KEY= 6af7067fcf01e468837330brkaf6545
//https://geocode.maps.co/search?q=Stockholm&api_key=6af7067fcf01e468837330brkaf6545


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GeocodeApi {
   // @GET("search?q={place}&api_key=6af7067fcf01e468837330brkaf6545")
    @GET("search")
    suspend fun getCoordinates(
        @Query("q") place: String,
        @Query("api_key") apiKey: String = "6af7067fcf01e468837330brkaf6545"
    ): List<GeocodeApiResponse>

}
@JsonClass(generateAdapter = true)
data class GeocodeApiResponse(
    @Json(name = "lon") val lon: Double,
    @Json(name = "lat") val lat: Double,
)