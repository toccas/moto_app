package com.moto.tracker.phone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_sessions")
data class RideSession(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val maxSpeedKmh: Float,
    val maxLeanAngleDeg: Float = 0f,
    val distanceKm: Float
)
