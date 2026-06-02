package com.moto.tracker.wear.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import com.moto.tracker.wear.TrackingViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TrackingScreen(viewModel: TrackingViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()

    val orange = Color(0xFFFF6B00)

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.startRide(context)
        else viewModel.setMessage("Permesso GPS negato")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (state.isTracking) {
            // --- Schermata tracking ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Velocità attuale
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.currentSpeedKmh.roundToInt()}",
                        color = orange,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp
                    )
                    Text("km/h", color = Color.Gray, fontSize = 11.sp)
                }

                // Statistiche
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    StatRow("Max", "${state.maxSpeedKmh.roundToInt()} km/h")
                    StatRow("Dist", "%.2f km".format(state.distanceKm))
                    StatRow("Tempo", formatDuration(state.elapsedSeconds))
                }

                // Bottone STOP — grande e centrale
                Button(
                    onClick = { viewModel.stopRide(context) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    modifier = Modifier.size(52.dp)
                ) {
                    Text("STOP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // --- Schermata iniziale ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MOTO\nTRACKER",
                    color = orange,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Messaggio errore/info
                if (message != null) {
                    Text(
                        text = message!!,
                        color = Color(0xFFFFCC00),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    LaunchedEffect(message) {
                        delay(4000)
                        viewModel.clearMessage()
                    }
                }

                // Bottone START — grande, arancione, al centro
                Button(
                    onClick = {
                        if (hasPermission) {
                            viewModel.startRide(context)
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = orange),
                    modifier = Modifier.size(64.dp)
                ) {
                    Text(
                        text = "START",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
