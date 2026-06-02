package com.moto.tracker.shared

import com.google.gson.Gson
import java.util.UUID

data class RideData(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val durationSeconds: Long = 0L,
    val maxSpeedKmh: Float = 0f,
    val distanceKm: Float = 0f
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        const val MESSAGE_PATH = "/ride_session"
        fun fromJson(json: String): RideData = Gson().fromJson(json, RideData::class.java)
    }
}
