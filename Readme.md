# Weather

An Android weather app built with **Jetpack Compose** and an **MVVM** architecture.
Search for any place, get a **7-day forecast** from the [SMHI open-data API](https://opendata.smhi.se),
tap a day for **hourly detail**, and save **favourite places** that persist on disk.

- Language: Kotlin (100%)
- UI: Jetpack Compose + Material 3
- Architecture: MVVM with manual dependency injection (no Hilt)
- Min SDK 24, target/compile SDK 37

---

## Architecture overview

The app is a single-activity Compose app organized in four layers:
`UI → ViewModels → Repositories → (network APIs / DataStore)`, plus a small
domain-model file. Dependencies are provided manually through a custom
`Application` class and ViewModel `Factory` objects.

**Navigation is manual**, not Navigation-Compose: `WeatherApp()` in
`MainActivity.kt` keeps `currentScreen: String` and `currentDay: Int` in
`remember { mutableStateOf(...) }` and switches screens with a `when`.
`DetailsScreen` installs a `BackHandler` so the system Back button calls
`onGoHome`.

---

# Project structure

## Key files and what they're responsible for

### App entry & navigation
| File | Responsibility |
|---|---|
| `MainActivity.kt` | `ComponentActivity` that calls `setContent { WeatherTheme { WeatherApp() } }`. `WeatherApp()` owns screen state (`currentScreen`, `currentDay`), creates the shared `HomeVM` via `viewModel(factory = HomeVM.Factory)`, and routes callbacks between screens. |
| `WeatherApplication.kt` | Custom `Application` (registered in the manifest) used for **manual DI**: creates the `UserPreferencesRepository` over a DataStore file named `game_preferences`. |

### UI layer (`ui/screens`)
| File | Responsibility |
|---|---|
| `HomeScreen.kt` | Place search field + "Search" button, the 7-day forecast `LazyColumn` (`ForecastList`/`ForecastRow` cards), a horizontally scrolling `LazyRow` of favourite-place buttons, and the "Settings" button. Reads `vm.forecast` / `vm.favourites` via `collectAsState()`. |
| `DetailsScreen.kt` | Hourly forecast for the selected day. Defines its own `ForecastList`/`ForecastRow` (note: duplicated with HomeScreen), and a `BackHandler { onGoHome() }`. Reads `vm.detailedForecast`. |
| `SettingsScreen.kt` | Scrollable `LazyColumn` of favourites with per-row "Remove", an `OutlinedTextField` + "Add favourite", "Save favourites" (persists), and "Home" (`onGetHome`) buttons. |
| `Functions.kt` | `CloudIcon(cloudAreaFraction)` – sun/cloud icon per forecast row. |
| `ui/theme/` | Material 3 theming (`WeatherTheme`, color schemes, typography). |

### ViewModels (`ui/viewmodels`)
| File | Responsibility |
|---|---|
| `HomeVM.kt` | `HomeViewModel` interface + `HomeVM` + `Forecast` data class. Exposes `forecast`, `detailedForecast`, `favourites`, `region`, `error` as `StateFlow`s. `search(place)` geocodes via `RegionRepository` then calls `search(lat, lon)` which fetches SMHI data via `WeatherRepository`; `makeForecast()` aggregates the time series into 7 daily averages; `getWeatherDetails(day)` filters one day for the details screen. In `init` it collects the saved response and favourites from `UserPreferencesRepository`. Errors are caught and surfaced as `error` strings. Companion `Factory` builds it from `WeatherApplication`. |
| `SettingsVM.kt` | `SettingsViewModel` interface + `SettingsVM`: in-memory favourite list with `addFavourite`/`removeFavourite`, `saveFavourites()` writing to DataStore, and an `init` collector keeping the list in sync with the repository flow. |

### Data layer (`data`)
| File | Responsibility |
|---|---|
| `SmhiApi.kt` | Retrofit interface `SmhiApi` for the SMHI point-forecast endpoint (`…/lon/{lon}/lat/{lat}/data.json`) plus the Moshi DTOs: `SmhiForecastResponse`, `SmhiGeometry`, `SmhiTimePoint`, `SmhiData` (with `@Json` snake_case mapping). |
| `GeocodeApi.kt` | Retrofit interface `GeocodeApi` for geocode.maps.co (`search?q=…&api_key=…`) + `GeocodeApiResponse(lon, lat)`. |
| `WeatherRepository.kt` | Wraps `SmhiApi`; `fetchForecast(lat, lon)` formats coordinates and calls the API. `create()` builds the Retrofit/Moshi stack (base URL `opendata-download-metfcst.smhi.se`). |
| `RegionRepository.kt` | Wraps `GeocodeApi`; `fetchCoordinates(placeName)` returns the first result's lon/lat. `create()` builds its Retrofit stack (base URL `geocode.maps.co`). |
| `GeocodeResponseData.kt` | Thin domain wrapper exposing `lat`/`lon` from a `GeocodeApiResponse`. |
| `SmhiResponseData.kt` | Domain model: `DateTime` (parses ISO-8601 strings into year/month/day/hour/…, `Comparable`), `SmhiTimeSeries` (flattens a time point into `time`, `temp`, `windDirection`, `cloudAreaFraction`), and `SmhiResponseData` (wraps the DTO response and derives `createdTime`/`referenceTime`/`timeSeries`). |
| `UserPreferencesRepository.kt` | DataStore-backed persistence: `favourites: Flow<Set<String>>` (string-set key), `savedData: Flow<SmhiResponseData?>` (last forecast stored as JSON via Moshi, key `response_json`), plus `saveFavourites()` / `saveResponse()`. Read errors are caught and fall back to empty preferences. |

---

## How the layers connect (data flow)

1. **Search by place** — `HomeScreen` Search button → `HomeVM.search("Stockholm")`
   → `RegionRepository.fetchCoordinates` → geocode.maps.co → lat/lon
   → `HomeVM.search(lat, lon)` → `WeatherRepository.fetchForecast` → SMHI API
   → Moshi parses JSON into `SmhiForecastResponse` → wrapped in `SmhiResponseData`
   → `makeForecast()` averages each of the next 7 days → `_forecast.value`
   → `HomeScreen` recomposes via `collectAsState()`. Failures set
   `error` (`"Failed to load Coordinates: …"` / `"Failed to load weather: …"`).
2. **Day details** — tapping a forecast card → `onGetWeather(day)` →
   `HomeVM.getWeatherDetails(day)` filters that day's points, sorts them, sets
   `_detailedForecast` → `WeatherApp` switches to `"details"` → `DetailsScreen`
   renders the hourly list.
3. **Favourites** — `SettingsScreen` add/remove updates `SettingsVM._favourites`;
   "Save favourites" → `UserPreferencesRepository.saveFavourites` → DataStore;
   the `favourites` Flow is collected by both `SettingsVM` and `HomeVM`, so the
   home screen's `LazyRow` of favourite buttons appears/updates automatically.
4. **Persistence across restarts** — on launch, `HomeVM.init` collects
   `savedData` (the last SMHI response, re-parsed from JSON) and calls
   `makeForecast()`, so the forecast is restored without a network call;
   `makeForecast()` also saves each new response back via `saveResponse`.

## Dependencies (app/build.gradle.kts)

- Compose BOM, `activity-compose`, `material3`, `ui`, `ui-tooling(-preview)`, material icons
- `lifecycle-runtime-ktx`, `lifecycle-viewmodel-compose` (ViewModels + `viewModel()`)
- `datastore-preferences` (persistence)
- `retrofit2` + `converter-moshi` + `moshi-kotlin` (networking/JSON)
- Tests: JUnit 4 (`test`), Compose UI test + Espresso (`androidTest`)

## Build & run

1. Open the project in **Android Studio** (or use `./gradlew`).
2. Run the `app` configuration on an emulator or device (min SDK 24).
3. Requires internet (`INTERNET` permission is declared). The geocode API key is
   hard-coded in `GeocodeApi.kt`.

Unit tests: `./gradlew testDebugUnitTest` — `ExampleUnitTest` covers SMHI JSON
parsing (Moshi) and the DTO→domain transformation with a sample payload.

## Notes / quirks

- Navigation is hand-rolled state in `WeatherApp()` (no Navigation-Compose), so
  there is no back stack; `DetailsScreen` uses `BackHandler` for system Back.
- `ForecastList`/`ForecastRow` are defined in **both** `HomeScreen.kt` and
  `DetailsScreen.kt` (same package — only one resolves per call site).
- `DateTime.compareTo` uses a 30-day-month approximation (and its `other` term
  is missing a `*3600` on the year), so ordering is only approximate.
- `HomeVM` has unused/TODO members (`setRegion`, `region`, `dataLoaded`) left
  over from prototyping.