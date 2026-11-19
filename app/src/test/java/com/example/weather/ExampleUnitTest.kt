package com.example.weather

import org.junit.Test

import org.junit.Assert.*

import com.example.weather.data.SmhiForecastResponse
import com.example.weather.data.SmhiResponseData
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail


class ExampleUnitTest {

    // Minimal but realistic SMHI opendata payload (same shape as .../data.json)
    private val sampleJson = """
        {
          "createdTime": "2024-05-06T10:00:00Z",
          "referenceTime": "2024-05-06T09:00:00Z",
          "geometry": { "type": "Point", "coordinates": [18.069, 59.329] },
          "timeSeries": [
            {
              "time": "2024-05-06T12:00:00Z",
              "intervalParametersStartTime": "2024-05-06T12:00:00Z",
              "data": { "air_temperature": 12.5, "wind_from_direction": 180, "cloud_area_fraction": 4 }
            },
            {
              "time": "2024-05-07T18:00:00Z",
              "intervalParametersStartTime": "2024-05-07T18:00:00Z",
              "data": { "air_temperature": 9.0, "wind_from_direction": 90, "cloud_area_fraction": 8 }
            }
          ]
        }
    """.trimIndent()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun json_parsing() {
        // Parse raw JSON into the API DTOs with Moshi
        val adapter = moshi.adapter(SmhiForecastResponse::class.java)
        val response = adapter.fromJson(sampleJson)
        assertNotNull(response)
        response!!

        // Top-level fields
        assertEquals("2024-05-06T10:00:00Z", response.createdTime)
        assertEquals("2024-05-06T09:00:00Z", response.referenceTime)
        assertEquals("Point", response.geometry.type)
        assertEquals(listOf(18.069, 59.329), response.geometry.coordinates)

        // timeSeries entries, incl. snake_case -> camelCase @Json mapping
        assertEquals(2, response.timeSeries.size)
        val first = response.timeSeries[0]
        assertEquals("2024-05-06T12:00:00Z", first.time)
        assertEquals(12.5, first.data.temp, 1e-9)          // air_temperature
        assertEquals(180, first.data.windDirection)          // wind_from_direction
        assertEquals(4, first.data.cloudAreaFraction)        // cloud_area_fraction
        assertEquals(9.0, response.timeSeries[1].data.temp, 1e-9)

        // Malformed JSON must fail, not silently parse
        try {
            adapter.fromJson("{ this is not json")
            fail("malformed json should throw")
        } catch (e: JsonEncodingException) {
            // expected
        }
    }

    @Test
    fun data_transformation() {
        // DTO -> domain model: SmhiResponseData derives DateTime fields and
        // flattens each SmhiTimePoint into SmhiTimeSeries(temp, windDirection, cloudAreaFraction)
        val response = moshi.adapter(SmhiForecastResponse::class.java).fromJson(sampleJson)!!
        val data = SmhiResponseData(response)

        // createdTime string becomes a parsed DateTime
        assertEquals(2024, data.createdTime.year)
        assertEquals(5, data.createdTime.month)
        assertEquals(6, data.createdTime.day)
        assertEquals(10, data.createdTime.hour)

        // timeSeries is mapped and flattened
        assertEquals(2, data.timeSeries.size)
        val first = data.timeSeries[0]
        assertEquals(12.5, first.temp, 1e-9)
        assertEquals(180, first.windDirection)
        assertEquals(4, first.cloudAreaFraction)
        assertEquals(6, first.time.day)
        assertEquals(12, first.time.hour)
        assertEquals(18, data.timeSeries[1].time.hour)
        assertEquals(8, data.timeSeries[1].cloudAreaFraction)
    }
}