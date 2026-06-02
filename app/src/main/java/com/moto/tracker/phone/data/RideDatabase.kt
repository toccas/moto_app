package com.moto.tracker.phone.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RideSession::class], version = 1, exportSchema = false)
abstract class RideDatabase : RoomDatabase() {
    abstract fun rideDao(): RideDao

    companion object {
        @Volatile private var INSTANCE: RideDatabase? = null

        fun getInstance(context: Context): RideDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, RideDatabase::class.java, "ride_db")
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
