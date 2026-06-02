package com.moto.tracker.phone.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route_points",
    foreignKeys = [
        ForeignKey(
            entity = RideSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class RoutePoint(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float,
    val leanAngleDeg: Float = 0f,
    val timestamp: Long
)
