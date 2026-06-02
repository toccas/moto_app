package com.moto.tracker.phone.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM ride_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<RideSession>>

    @Query("SELECT * FROM ride_sessions WHERE id = :id")
    suspend fun getSession(id: String): RideSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RideSession)

    @Delete
    suspend fun delete(session: RideSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoints(points: List<RoutePoint>)

    @Query("SELECT * FROM route_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getRoutePoints(sessionId: String): List<RoutePoint>
}
