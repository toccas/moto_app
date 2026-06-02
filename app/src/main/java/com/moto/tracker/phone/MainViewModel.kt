package com.moto.tracker.phone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.moto.tracker.phone.data.RideDatabase
import com.moto.tracker.phone.data.RideSession
import com.moto.tracker.phone.data.RoutePoint
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = RideDatabase.getInstance(app).rideDao()

    val sessions: Flow<List<RideSession>> = dao.getAllSessions()

    suspend fun deleteSession(session: RideSession) = dao.delete(session)
    suspend fun getSession(id: String): RideSession? = dao.getSession(id)
    suspend fun getRoutePoints(sessionId: String): List<RoutePoint> = dao.getRoutePoints(sessionId)

    suspend fun insertDebugSession() {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val startTime = now - 47 * 60 * 1000L
        val endTime = now - 3 * 60 * 1000L

        // Percorso ovale attorno a Milano (Navigli → Lambrate → Loreto → Navigli)
        val centerLat = 45.4564
        val centerLon = 9.1826
        val numPoints = 90
        val durationMs = endTime - startTime

        val routePoints = (0 until numPoints).map { i ->
            val t = i.toFloat() / numPoints
            val angle = t * 2.0 * PI

            // Forma leggermente irregolare per sembrare una strada reale
            val lat = centerLat + 0.018 * sin(angle) + 0.003 * sin(3 * angle) + 0.001 * sin(7 * angle)
            val lon = centerLon + 0.028 * cos(angle) + 0.004 * cos(2 * angle) + 0.002 * cos(5 * angle)

            // Profilo velocità: lento all'inizio/fine (zona città), veloce a metà (tangenziale)
            val speedKmh: Float = when {
                t < 0.08f || t > 0.92f -> 15f + 8f * (i % 3)
                t < 0.20f || t > 0.80f -> 35f + 15f * (i % 4)
                t < 0.35f || t > 0.65f -> 65f + 20f * (i % 5)
                else -> 95f + 35f * (i % 4).toFloat()
            }

            // Inclinazione: alta nelle curve (inizio/fine segmenti veloci), bassa nei rettilinei
            val leanAngleDeg: Float = when {
                t < 0.08f || t > 0.92f -> 5f + 3f * (i % 3)
                t < 0.22f || t > 0.78f -> 18f + 8f * (i % 4)
                t < 0.28f || t > 0.72f -> 38f + 10f * (i % 3)         // curve di raccordo
                t < 0.36f || t > 0.64f -> 12f + 5f * (i % 5)          // rettilineo scorrimento
                else -> 8f + 4f * (i % 6).toFloat()                    // tangenziale (rettilineo)
            }

            RoutePoint(
                sessionId = id,
                latitude = lat,
                longitude = lon,
                speedKmh = speedKmh,
                leanAngleDeg = leanAngleDeg,
                timestamp = startTime + (i * durationMs / numPoints)
            )
        }

        val maxSpeed = routePoints.maxOf { it.speedKmh }
        val maxLean = routePoints.maxOf { it.leanAngleDeg }

        val session = RideSession(
            id = id,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = (endTime - startTime) / 1000L,
            maxSpeedKmh = maxSpeed,
            maxLeanAngleDeg = maxLean,
            distanceKm = 38.4f
        )

        dao.insert(session)
        dao.insertRoutePoints(routePoints)
    }
}
