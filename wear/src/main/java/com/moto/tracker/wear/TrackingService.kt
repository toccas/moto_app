package com.moto.tracker.wear

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackingService : Service() {

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private var lastLocation: Location? = null
    private var totalDistanceMeters = 0f
    private var maxSpeedMs = 0f
    private var startTime = 0L

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onNewLocation(it) }
            }
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // API 30+ richiede di specificare il tipo di servizio in primo piano
        startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        startTime = System.currentTimeMillis()
        // Segna subito come in tracking — non aspettare il primo fix GPS
        _state.value = TrackingState(isTracking = true)
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
            Log.d(TAG, "Location updates started")
        } catch (e: Exception) {
            Log.e(TAG, "requestLocationUpdates failed: ${e.message}")
            _state.value = TrackingState(isTracking = false)
        }
    }

    private fun onNewLocation(location: Location) {
        val speedMs = if (location.hasSpeed()) location.speed else 0f
        if (speedMs > maxSpeedMs) maxSpeedMs = speedMs

        lastLocation?.let { prev -> totalDistanceMeters += prev.distanceTo(location) }
        lastLocation = location

        val elapsed = (System.currentTimeMillis() - startTime) / 1000L
        _state.value = TrackingState(
            isTracking = true,
            currentSpeedKmh = speedMs * 3.6f,
            maxSpeedKmh = maxSpeedMs * 3.6f,
            distanceKm = totalDistanceMeters / 1000f,
            elapsedSeconds = elapsed
        )
    }

    fun stopAndGetResult(): TrackingResult {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val endTime = System.currentTimeMillis()
        return TrackingResult(
            startTime = startTime,
            endTime = endTime,
            durationSeconds = (endTime - startTime) / 1000L,
            maxSpeedKmh = maxSpeedMs * 3.6f,
            distanceKm = totalDistanceMeters / 1000f
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Moto Tracker", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Moto Tracker")
            .setContentText("Registrazione in corso...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "tracking_channel"
    }
}

data class TrackingState(
    val isTracking: Boolean = false,
    val currentSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val distanceKm: Float = 0f,
    val elapsedSeconds: Long = 0L
)

data class TrackingResult(
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val maxSpeedKmh: Float,
    val distanceKm: Float
)
