package com.moto.tracker.wear

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.Wearable
import com.moto.tracker.shared.RideData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TrackingViewModel(app: Application) : AndroidViewModel(app) {

    private var trackingService: TrackingService? = null

    private val _uiState = MutableStateFlow(TrackingState())
    val uiState: StateFlow<TrackingState> = _uiState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as TrackingService.LocalBinder).getService()
            trackingService = service
            viewModelScope.launch {
                service.state.collect { _uiState.value = it }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            _uiState.value = TrackingState()
        }
    }

    fun startRide(context: Context) {
        if (trackingService != null) return  // già in tracking
        try {
            val intent = Intent(context, TrackingService::class.java)
            context.startForegroundService(intent)
            val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d("TrackingViewModel", "startForegroundService called, bind result: $bound")
            if (!bound) _message.value = "Errore: bind al servizio fallito"
        } catch (e: Exception) {
            Log.e("TrackingViewModel", "startRide failed", e)
            _message.value = "Errore: ${e.message}"
        }
    }

    fun stopRide(context: Context) {
        val result = trackingService?.stopAndGetResult() ?: return
        try {
            context.unbindService(serviceConnection)
        } catch (_: Exception) {}
        context.stopService(Intent(context, TrackingService::class.java))
        trackingService = null
        _uiState.value = TrackingState()
        sendToPhone(context, result)
    }

    fun setMessage(msg: String) { _message.value = msg }
    fun clearMessage() { _message.value = null }

    private fun sendToPhone(context: Context, result: TrackingResult) {
        val rideData = RideData(
            id = UUID.randomUUID().toString(),
            startTime = result.startTime,
            endTime = result.endTime,
            durationSeconds = result.durationSeconds,
            maxSpeedKmh = result.maxSpeedKmh,
            distanceKm = result.distanceKm
        )
        viewModelScope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                if (nodes.isEmpty()) {
                    _message.value = "Telefono non connesso"
                    return@launch
                }
                val payload = rideData.toJson().toByteArray()
                nodes.forEach { node ->
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, RideData.MESSAGE_PATH, payload)
                        .await()
                }
                _message.value = "Giro salvato! %.1f km".format(result.distanceKm)
            } catch (e: Exception) {
                Log.e("TrackingViewModel", "sendToPhone failed", e)
                _message.value = "Errore invio: ${e.message}"
            }
        }
    }
}
