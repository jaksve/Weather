package com.example.weather.data


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
){
    // val HIGHSCORE = intPreferencesKey("highscore")

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val responseAdapter = moshi.adapter(SmhiForecastResponse::class.java)



    private companion object {
        val RESPONSE_JSON = stringPreferencesKey("response_json")
        val FAVOURITES = stringSetPreferencesKey("favourites")
        const val TAG = "UserPreferencesRepo"
    }
    //should save a list of 7 day forecasts MutableStateFlow<List<Forecast>>
    // as well as the detailedforecasts MutableStateFlow<List<Forecast>>
    // All of the above exists in one way or another in SMHIresponseData. although we need to remake makeForecast()
    //or just call it from the init??
    val favourites : Flow<Set<String>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[FAVOURITES] ?: emptySet()
        }

    val savedData : Flow<SmhiResponseData?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[RESPONSE_JSON]?.let {json ->
                try {
                    SmhiResponseData(responseAdapter.fromJson(json)!!)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error parsing json", e)
                    null
                }
            }
        }

    suspend fun saveFavourites(favourites: Set<String>) {
        dataStore.edit { preferences ->
            preferences[FAVOURITES] = favourites
        }
    }
    suspend fun saveResponse(response: SmhiResponseData) {
        dataStore.edit { preferences ->
            preferences[RESPONSE_JSON] =responseAdapter.toJson(response.response)        }
    }
}