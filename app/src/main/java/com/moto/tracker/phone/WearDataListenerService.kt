package com.moto.tracker.phone

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.moto.tracker.phone.data.RideDatabase
import com.moto.tracker.phone.data.RideSession
import com.moto.tracker.shared.RideData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearDataListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != RideData.MESSAGE_PATH) return

        val rideData = RideData.fromJson(String(event.data))
        val session = RideSession(
            id = rideData.id,
            startTime = rideData.startTime,
            endTime = rideData.endTime,
            durationSeconds = rideData.durationSeconds,
            maxSpeedKmh = rideData.maxSpeedKmh,
            distanceKm = rideData.distanceKm
        )

        scope.launch {
            RideDatabase.getInstance(applicationContext).rideDao().insert(session)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
