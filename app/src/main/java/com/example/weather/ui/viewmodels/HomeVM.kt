package com.example.weather.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weather.WeatherApplication
import com.example.weather.data.DateTime
import com.example.weather.data.GeocodeResponseData
import com.example.weather.data.RegionRepository
import com.example.weather.data.SmhiResponseData
import com.example.weather.data.SmhiTimeSeries
import com.example.weather.data.UserPreferencesRepository
import com.example.weather.data.WeatherRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Comparator

//Lets the user enter a city name or geographic coordinates, and shows the
//current conditions and a 7-day forecast summary.

//Fetch forecast data from open-meteo.com or the SMHI API.
interface HomeViewModel{
    val favourites : MutableStateFlow<List<String>>
    val region : StateFlow<String>
    val forecast: MutableStateFlow<List<Forecast>>
    val error: StateFlow<String?>
    val detailedForecast : MutableStateFlow<List<Forecast>>
    fun setRegion(region:String)
    fun search(lat: Double, lon: Double)
    fun search (region: String)
    fun loadFavourites()

    fun getWeatherDetails(day: Int)

}

data class Forecast(
    val time: DateTime,
    val temp: Double,
    val windDirection: Int,
    val cloudAreaFraction: Int
)
class HomeVM (private val userPreferencesRepository: UserPreferencesRepository,
              private val place : String,
              private val weatherRepository: WeatherRepository = WeatherRepository.create(),
              private val regionRepository: RegionRepository = RegionRepository.create()
): HomeViewModel, ViewModel() {
    private val _favourites =  MutableStateFlow<List<String>>(emptyList())
    override val favourites : MutableStateFlow<List<String>>
        get() {
            return _favourites
        }
    private val dataLoaded = CompletableDeferred<Unit>()
    private var smhiResponseData: SmhiResponseData? = null
    private var GeocodeResponseData : GeocodeResponseData? = null
    private val _forecast = MutableStateFlow<List<Forecast>>(emptyList())
    override val forecast: MutableStateFlow<List<Forecast>>
        get() {
            return _forecast
        }
    private val _detailedForecast = MutableStateFlow<List<Forecast>>(emptyList())
    override val detailedForecast : MutableStateFlow<List<Forecast>>
        get(){
            return _detailedForecast
        }


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
    override fun getWeatherDetails(day: Int)
    {
        val forecasts = smhiResponseData!!.timeSeries.filter {it.time.day == day}
        Log.d("HomeVM: getWeatherDetails", "getWeatherDetails, day: $day, forecasts: $forecasts")

        val detailedForecast = mutableListOf<Forecast>()
        forecasts.forEach {detailedForecast.add(Forecast(it.time,it.temp,it.windDirection,it.cloudAreaFraction)) }
        detailedForecast.sortWith(Comparator { o1, o2 -> o2.time.compareTo(o1.time)})
        _detailedForecast.value = detailedForecast

        Log.d("HomeVM: getWeatherDetails", "getWeatherDetails, day: $day, detailedForecast: ${_detailedForecast.value}")
    }
    private suspend fun makeForecast()
    {
        //dataLoaded.await()
        if(smhiResponseData == null || smhiResponseData?.timeSeries== null || smhiResponseData?.timeSeries?.isEmpty() == true) {
            _forecast.value = emptyList()
            return
        }
        viewModelScope.launch { userPreferencesRepository.saveResponse(smhiResponseData!!) }
        val forecastList = mutableListOf<Forecast>()
        Log.d("HomeVM", "makeForecast, responseData created Day: ${smhiResponseData?.createdTime?.day}")
        val firstDay : DateTime = smhiResponseData!!.timeSeries.minOf { it.time } ?: DateTime("2023-01-01T00:00:00Z")
        for(i in 0..6) // Iterate over the next 7 days
        {
            val currentDay: List<SmhiTimeSeries> = smhiResponseData!!.timeSeries.filter {it.time.day == firstDay.day+i}
            val temp = ((currentDay.sumOf { it.temp }) / currentDay.size)
            val windDirection = ((currentDay.sumOf { it.windDirection }) / currentDay.size)
            val cloudAreaFraction = ((currentDay.sumOf { it.cloudAreaFraction }) / currentDay.size)
            val time = firstDay.copy(day = firstDay.day+i)
            forecastList.add(i,Forecast(time,temp, windDirection, cloudAreaFraction ))
        }
        _forecast.value = forecastList
    }
    //Coordinate input accepts only valid floating-point values.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun search(lat : Double, lon : Double)
    {
        Log.d("HomeVM: search", "Favourites: ${_favourites.value}")
        viewModelScope.launch {
            _error.value = null
            try {
                Log.d("HomeVM: search", "search, lat: $lat, lon: $lon")
                val response = weatherRepository.fetchForecast(lat, lon)
                Log.d("HomeVM: search", "Response Received!")
                smhiResponseData = SmhiResponseData(response)
                dataLoaded.complete(Unit)
                Log.d("HomeVM: search", "data Loaded complete")
                makeForecast()

            } catch (e: Exception) {
                _error.value = "Failed to load weather: ${e.message}"
                Log.d("HomeVM", "error : ${_error.value}")
            }
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun search(region: String)
    {
        //TODO
        viewModelScope.launch {
            _error.value = null
            try {
                Log.d("HomeVM: RegionSearch", "search, $region")
                val response = regionRepository.fetchCoordinates(region)
                Log.d("HomeVM: RegionSearch", "search, $response")
                GeocodeResponseData = GeocodeResponseData(response)

                Log.d("HomeVM: RegionSearch", "data Loaded lat:${GeocodeResponseData!!.lat} lon:${GeocodeResponseData!!.lon}")

                search(GeocodeResponseData!!.lat,GeocodeResponseData!!.lon)

            } catch (e: Exception) {
                _error.value = "Failed to load Coordinates: ${e.message}"
                Log.d("HomeVM", "error : ${_error.value}")
            }
           // val response = repository.fetchForecast(lat, lon)
        }
    }
    override fun loadFavourites() {
        viewModelScope.launch {
            userPreferencesRepository.favourites.collect {
                _favourites.value = it.toList()
            }
        }
    }

    init{
        viewModelScope.launch {
            Log.d("HomeVM init", "init")
            userPreferencesRepository.savedData.collect {
                smhiResponseData = it
                makeForecast()
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.favourites.collect{
                _favourites.value = it.toList()
            }
            Log.d("HomeVM init", "Favourites: ${_favourites.value}")
        }
    }
    companion object
    {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                Log.d("HomeVM", "Factory")
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                HomeVM(application.userPreferencesRepository,"London")
            }
        }
    }

}