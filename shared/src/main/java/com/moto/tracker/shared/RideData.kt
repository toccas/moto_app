package com.moto.tracker.shared

import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float,
    val leanAngleDeg: Float = 0f,
    val timestamp: Long
)

data class RideData(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val durationSeconds: Long = 0L,
    val maxSpeedKmh: Float = 0f,
    val maxLeanAngleDeg: Float = 0f,
    val distanceKm: Float = 0f,
    val gpsPoints: List<GpsPoint> = emptyList()
) {
    fun toJson(): String = Gson().toJson(this)

    fun toGzipBytes(): ByteArray {
        val json = toJson()
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { it.write(json.toByteArray(Charsets.UTF_8)) }
        return baos.toByteArray()
    }

    companion object {
        const val MESSAGE_PATH = "/ride_session"

        fun fromJson(json: String): RideData = Gson().fromJson(json, RideData::class.java)

        fun fromGzipBytes(bytes: ByteArray): RideData {
            val json = GZIPInputStream(ByteArrayInputStream(bytes)).use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            return fromJson(json)
        }
    }
}
