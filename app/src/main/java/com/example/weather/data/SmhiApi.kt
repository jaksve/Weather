// api/SmhiApi.kt
package com.example.weather.data
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface SmhiApi {
    @GET("api/category/snow1g/version/1/geotype/point/lon/{lon}/lat/{lat}/data.json") // https://opendata-download-metfcst.smhi.se/api/category/snow1g/version/1/geotype/point/lon/18.069/lat/59.329/data.json
    suspend fun getPointForecast(
        @Path("lon") lon: String,
        @Path("lat") lat: String
    ): SmhiForecastResponse
}
// api/SmhiDtos.kt




@JsonClass(generateAdapter = true)
data class SmhiForecastResponse(
    @Json(name = "createdTime") val createdTime: String,
    @Json(name = "referenceTime") val referenceTime: String,
    @Json(name = "geometry") val geometry: SmhiGeometry,
    @Json(name = "timeSeries") val timeSeries: List<SmhiTimePoint>
)
{
    fun param(temp: Double): SmhiTimePoint? = timeSeries.firstOrNull() { it.data.temp ==temp }
}
@JsonClass(generateAdapter = true)
data class SmhiGeometry(
    @Json(name = "type") val type: String,
    @Json(name = "coordinates") val coordinates: List<Double>
)

@JsonClass(generateAdapter = true)
data class SmhiTimePoint(
    @Json(name = "time") val time: String,
    @Json(name = "intervalParametersStartTime") val intervalParametersStartTime: String,
    @Json(name = "data") val data: SmhiData
) {
 //   fun param(temp: Double): SmhiData? = data.firstOrNull { it.temp ==temp }
}

@JsonClass(generateAdapter = true)
data class SmhiData(
    @Json(name = "air_temperature") val temp: Double,
    @Json(name = "wind_from_direction") val windDirection: Int,
    @Json(name = "cloud_area_fraction") val cloudAreaFraction: Int,
)