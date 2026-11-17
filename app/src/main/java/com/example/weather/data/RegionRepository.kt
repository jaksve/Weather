package com.example.weather.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RegionRepository (private val api: GeocodeApi){ //https://www.smhi.se/wpta/backend_solr/autocomplete/search/{place}

    suspend fun fetchCoordinates(placeName : String): GeocodeApiResponse {
        Log.d("RegionRepository", "Fetching coordinates for place: $placeName")
        return  api.getCoordinates(
            place = placeName).first()

    }
    companion object {
        fun create(): RegionRepository {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://geocode.maps.co/") //https://maceo.sth.kth.se/weather/forecast?lonLat=lon/14.333/lat/60.383
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return RegionRepository(retrofit.create(GeocodeApi::class.java))
        }
    }
}