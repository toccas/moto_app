package com.moto.tracker.phone.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RideSession::class, RoutePoint::class], version = 3, exportSchema = false)
abstract class RideDatabase : RoomDatabase() {
    abstract fun rideDao(): RideDao

    companion object {
        @Volatile private var INSTANCE: RideDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `route_points` (
                        `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `speedKmh` REAL NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`sessionId`) REFERENCES `ride_sessions`(`id`) ON DELETE CASCADE
                    )"""
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_route_points_sessionId` ON `route_points` (`sessionId`)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `ride_sessions` ADD COLUMN `maxLeanAngleDeg` REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `route_points` ADD COLUMN `leanAngleDeg` REAL NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): RideDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, RideDatabase::class.java, "ride_db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
