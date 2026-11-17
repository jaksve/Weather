package com.example.weather.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.ui.viewmodels.SettingsVM

@Composable
fun SettingsScreen(
    vm: SettingsVM,
    onGetHome: () -> Unit
) {
    //THEME -- colors
    //saved places.
    var newFavourite by remember { mutableStateOf("") }
    val favourites =vm.favourites.collectAsState()//by remember{ mutableStateOf(vm.favourites)}

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Settings")

            //scrollable list of favourite places
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(favourites.value.toList(), key = { it }) { favourite ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = favourite)
                        //remove a favourite
                        TextButton(onClick = { vm.removeFavourite(favourite) }) {
                            Text(text = "Remove")
                        }
                    }
                }
            }

            //text field to add a favourite place
            OutlinedTextField(
                value = newFavourite,
                onValueChange = { newFavourite = it },
                label = { Text(text = "New favourite place") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val place = newFavourite.trim()
                    if (place.isNotEmpty()) {
                        vm.addFavourite(place)
                        newFavourite = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add favourite")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //save favourites
                Button(
                    onClick = { vm.saveFavourites() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Save favourites")
                }
                //go home
                Button(
                    onClick = onGetHome,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Home")
                }
            }
        }
    }
}