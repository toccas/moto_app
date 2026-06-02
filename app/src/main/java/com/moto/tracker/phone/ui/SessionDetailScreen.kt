package com.moto.tracker.phone.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moto.tracker.phone.MainViewModel
import com.moto.tracker.phone.data.RideSession
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var session by remember { mutableStateOf<RideSession?>(null) }
    LaunchedEffect(sessionId) {
        session = viewModel.getSession(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio Giro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        session?.let { s ->
            SessionDetailContent(session = s, modifier = Modifier.padding(padding))
        } ?: Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }
}

@Composable
private fun SessionDetailContent(session: RideSession, modifier: Modifier = Modifier) {
    val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.ITALY)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALY)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dateFormat.format(Date(session.startTime)).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "${timeFormat.format(Date(session.startTime))} → ${timeFormat.format(Date(session.endTime))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Distanza — dato principale
        Text(
            text = "%.2f".format(session.distanceKm),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "chilometri",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBlock(
                icon = Icons.Filled.Speed,
                value = "${session.maxSpeedKmh.toInt()} km/h",
                label = "Velocità massima"
            )
            StatBlock(
                icon = Icons.Filled.Timer,
                value = formatDuration(session.durationSeconds),
                label = "Durata"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Velocità media
        val avgSpeed = if (session.durationSeconds > 0)
            (session.distanceKm / (session.durationSeconds / 3600.0)).toFloat()
        else 0f

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBlock(
                icon = Icons.Filled.TwoWheeler,
                value = "${avgSpeed.toInt()} km/h",
                label = "Velocità media"
            )
        }
    }
}

@Composable
private fun StatBlock(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
