package com.example.weather.data


data class DateTime (
    val time : String,
    val year : Int = time.substring(0,4).toInt(),
    val month : Int = time.substring(5,7).toInt(),
    val day: Int = time.substring(8,10).toInt(),
    val hour: Int = time.substring(11,13).toInt(),
    val minute: Int = time.substring(14,16).toInt(),
    val second: Int = time.substring(17,19).toInt()
): Comparable<DateTime> {
    override fun compareTo(other: DateTime): Int {
        val toNumber = second+minute*60+hour*3600+day*24*3600+month*30*24*3600+year*12*30*24*3600
        val otherNumber = other.second+other.minute*60+other.hour*3600+other.day*24*3600+other.month*30*24*3600+other.year*12*30*24
        return toNumber.compareTo(otherNumber)
    }
}

data class SmhiTimeSeries (
    val timeSeries : SmhiTimePoint,
    val time: DateTime = DateTime(timeSeries.time),
    val intervalParametersStartTime : DateTime = DateTime(timeSeries.intervalParametersStartTime),
    val temp : Double = timeSeries.data.temp,
    val windDirection : Int = timeSeries.data.windDirection,
    val cloudAreaFraction : Int = timeSeries.data.cloudAreaFraction
)
data class SmhiResponseData(
    val response : SmhiForecastResponse,
    val createdTime : DateTime = DateTime(response.createdTime),
    val referenceTime : DateTime = DateTime(response.referenceTime),
    val timeSeries : List<SmhiTimeSeries> = response.timeSeries.map { SmhiTimeSeries(it) }
)

