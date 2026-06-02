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
}
