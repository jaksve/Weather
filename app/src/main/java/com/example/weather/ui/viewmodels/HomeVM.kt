package com.example.weather.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weather.data.DateTime
import com.example.weather.data.ResponseData
import com.example.weather.data.SmhiTimeSeries
import com.example.weather.data.WeatherRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//Lets the user enter a city name or geographic coordinates, and shows the
//current conditions and a 7-day forecast summary.

//Fetch forecast data from open-meteo.com or the SMHI API.
interface HomeViewModel{

    val region : StateFlow<String>
    val forecast: MutableStateFlow<List<Forecast>>
    val error: StateFlow<String?>
    fun setRegion(region:String)
    fun search(lat: Double, lon: Double)

}

data class Forecast(
    val temp: Double,
    val windDirection: Int,
    val cloudAreaFraction: Int
)
class HomeVM (private val place : String,
              private val repository: WeatherRepository = WeatherRepository.create()
): HomeViewModel, ViewModel() {
    private val dataLoaded = CompletableDeferred<Unit>()
    private var responseData: ResponseData? = null
    private val _forecast = MutableStateFlow<List<Forecast>>(emptyList())
    override val forecast: MutableStateFlow<List<Forecast>>
        get() {
            return _forecast
        }
    //seven day forecast summary : temp and some visual representation of the weather conditions.
    //Several

    private val _region = MutableStateFlow<String> ("")
    override val region: StateFlow<String>
        get() = _region
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?>
        get(){
            return _error
        }

    override fun setRegion(region:String)
    {
        //TODO
    }
    private suspend fun makeForecast()
    {
        //dataLoaded.await()
        if(responseData == null || responseData?.timeSeries== null || responseData?.timeSeries?.isEmpty() == true) {
            _forecast.value = emptyList()
            return
        }
        val forecastList = mutableListOf<Forecast>()
        Log.d("HomeVM", "makeForecast, responseData created Day: ${responseData?.createdTime?.day}")
        val firstDay : DateTime = responseData!!.timeSeries.minOf { it.time } ?: DateTime("2023-01-01T00:00:00Z")
        for(i in 0..6) // Iterate over the next 7 days
        {
            val currentDay: List<SmhiTimeSeries> = responseData!!.timeSeries.filter {it.time.day == firstDay.day+i}
            val temp = ((currentDay.sumOf { it.temp }) / currentDay.size)
            val windDirection = ((currentDay.sumOf { it.windDirection }) / currentDay.size)
            val cloudAreaFraction = ((currentDay.sumOf { it.cloudAreaFraction }) / currentDay.size)
            forecastList.add(i,Forecast(temp, windDirection, cloudAreaFraction))
        }
        _forecast.value = forecastList
    }
    //Coordinate input accepts only valid floating-point values.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun search(lat : Double, lon : Double)
    {
        viewModelScope.launch {
            _error.value = null
            try {
                Log.d("HomeVM: search", "search, lat: $lat, lon: $lon")
                val response = repository.fetchForecast(lat, lon)
                Log.d("HomeVM: search", "Response Received!")
                responseData = ResponseData(response)
                dataLoaded.complete(Unit)
                Log.d("HomeVM: search", "data Loaded complete")
                makeForecast()

            } catch (e: Exception) {
                _error.value = "Failed to load weather: ${e.message}"
                Log.d("HomeVM", "error : ${_error.value}")
            }
        }

    }

    init{
        _region.value = place
    }
    companion object
    {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeVM(place = "London")
            }
        }
    }

}