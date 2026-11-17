package com.example.weather.ui.viewmodels
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weather.WeatherApplication
import com.example.weather.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

interface SettingsViewModel{
    val favourites : MutableStateFlow<List<String>>
    fun removeFavourite(item: String)
    fun saveFavourites()
    fun addFavourite(item: String)
}
class SettingsVM (private val userPreferencesRepository: UserPreferencesRepository):SettingsViewModel, ViewModel(){
    private val _favourites =  MutableStateFlow<List<String>>(emptyList())
    override val favourites : MutableStateFlow<List<String>>
        get() {
            return _favourites
        }
    override fun addFavourite(item: String)
    {
        val templist = mutableListOf<String> ()//_favourites.value.toMutableList()
        templist.addAll(_favourites.value)
        templist.add(item)
        _favourites.value = templist

        //_favourites.add(item)
    }
    override fun removeFavourite(item: String)
    {
        val templist = mutableListOf<String> ()//_favourites.value.toMutableList()
        templist.addAll(_favourites.value)
        templist.remove(item)
        _favourites.value = templist
    }

    override fun saveFavourites() {
        viewModelScope.launch{
            userPreferencesRepository.saveFavourites(_favourites.value.toSet())
        }
    }
    init{
        viewModelScope.launch {
            userPreferencesRepository.favourites.collect{
                _favourites.value = it.toList()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                SettingsVM(application.userPreferencesRepository)
            }
        }
    }
    //list of preffered places a factory and saves and loads from disc.
}