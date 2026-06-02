package com.moto.tracker.phone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.moto.tracker.phone.data.RideDatabase
import com.moto.tracker.phone.data.RideSession
import kotlinx.coroutines.flow.Flow

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = RideDatabase.getInstance(app).rideDao()

    val sessions: Flow<List<RideSession>> = dao.getAllSessions()

    suspend fun deleteSession(session: RideSession) = dao.delete(session)
    suspend fun getSession(id: String): RideSession? = dao.getSession(id)
}
