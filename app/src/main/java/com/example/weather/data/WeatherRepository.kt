// data/WeatherRepository.kt
package com.example.weather.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WeatherRepository(private val api: SmhiApi) {

     suspend fun fetchForecast(lat: Double, lon: Double): SmhiForecastResponse {
        val latString :String = String.format("%.3f", lat)
        val lonString :String = String.format("%.3f", lon)
        //Log.d("WeatherRepository", "latString : $latString, lonString : $lonString")
        return  api.getPointForecast(
            lon = lonString, lat = latString)

    }
    companion object {
        fun create(): WeatherRepository {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://opendata-download-metfcst.smhi.se/") //https://maceo.sth.kth.se/weather/forecast?lonLat=lon/14.333/lat/60.383
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return WeatherRepository(retrofit.create(SmhiApi::class.java))
        }
    }
}