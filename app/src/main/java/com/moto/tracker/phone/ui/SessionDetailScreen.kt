package com.moto.tracker.phone.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.moto.tracker.phone.MainViewModel
import com.moto.tracker.phone.data.RideSession
import com.moto.tracker.phone.data.RoutePoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.text.SimpleDateFormat
import java.util.*

private enum class MapMode { SPEED, LEAN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var session by remember { mutableStateOf<RideSession?>(null) }
    var routePoints by remember { mutableStateOf<List<RoutePoint>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(sessionId) {
        session = viewModel.getSession(sessionId)
        routePoints = viewModel.getRoutePoints(sessionId)
        loading = false
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
        when {
            loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            session != null -> SessionDetailContent(
                session = session!!,
                routePoints = routePoints,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun SessionDetailContent(
    session: RideSession,
    routePoints: List<RoutePoint>,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.ITALY)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALY)
    var mapMode by remember { mutableStateOf(MapMode.SPEED) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Toggle vista mappa
        if (routePoints.size >= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Vista: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(4.dp))
                FilterChip(
                    selected = mapMode == MapMode.SPEED,
                    onClick = { mapMode = MapMode.SPEED },
                    label = { Text("Velocità") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = mapMode == MapMode.LEAN,
                    onClick = { mapMode = MapMode.LEAN },
                    label = { Text("Inclinazione") }
                )
            }
        }

        RouteMap(
            routePoints = routePoints,
            mapMode = mapMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )

        if (routePoints.size >= 2) {
            MapLegend(
                mapMode = mapMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
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

            Spacer(Modifier.height(24.dp))

            Text(
                text = "%.2f".format(session.distanceKm),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "chilometri",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBlock(Icons.Filled.Speed, "${session.maxSpeedKmh.toInt()} km/h", "Velocità max")
                StatBlock(Icons.Filled.Timer, formatDuration(session.durationSeconds), "Durata")
            }

            Spacer(Modifier.height(20.dp))

            val avgSpeed = if (session.durationSeconds > 0)
                (session.distanceKm / (session.durationSeconds / 3600.0)).toFloat() else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBlock(Icons.Filled.TwoWheeler, "${avgSpeed.toInt()} km/h", "Velocità media")
                LeanStatBlock(session.maxLeanAngleDeg)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Mappa ─────────────────────────────────────────────────────────────────────

@Composable
private fun RouteMap(
    routePoints: List<RoutePoint>,
    mapMode: MapMode,
    modifier: Modifier = Modifier
) {
    if (routePoints.size < 2) {
        Box(
            modifier = modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Map, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Text("Percorso GPS non disponibile", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
        return
    }

    var selectedSegment by remember { mutableStateOf<RouteSegment?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(routePoints, mapMode) {
        mapView.overlays.clear()
        selectedSegment = null

        val segments = routePoints.zipWithNext().map { (a, b) ->
            RouteSegment(
                start = GeoPoint(a.latitude, a.longitude),
                end = GeoPoint(b.latitude, b.longitude),
                speedKmh = (a.speedKmh + b.speedKmh) / 2f,
                leanAngleDeg = (a.leanAngleDeg + b.leanAngleDeg) / 2f
            )
        }

        mapView.overlays.add(
            ColoredRouteOverlay(
                segments = segments,
                colorFor = { seg ->
                    if (mapMode == MapMode.SPEED) speedToAndroidColor(seg.speedKmh)
                    else leanToAndroidColor(seg.leanAngleDeg)
                },
                onSegmentTapped = { seg -> selectedSegment = seg },
                onMapTapped = { selectedSegment = null }
            )
        )

        mapView.post {
            val bbox = BoundingBox.fromGeoPoints(routePoints.map { GeoPoint(it.latitude, it.longitude) })
            mapView.zoomToBoundingBox(bbox, true, 80)
            mapView.invalidate()
        }
    }

    Box(modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
        )

        // Bottoni zoom
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { mapView.controller.zoomIn() },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) { Icon(Icons.Filled.Add, "Zoom in") }

            SmallFloatingActionButton(
                onClick = { mapView.controller.zoomOut() },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) { Icon(Icons.Filled.Remove, "Zoom out") }
        }

        // Tooltip tocco segmento
        selectedSegment?.let { seg ->
            SegmentTooltip(
                seg = seg,
                mapMode = mapMode,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
            )
        }
    }
}

// ── Overlay personalizzato ────────────────────────────────────────────────────

private data class RouteSegment(
    val start: GeoPoint,
    val end: GeoPoint,
    val speedKmh: Float,
    val leanAngleDeg: Float
)

private class ColoredRouteOverlay(
    private val segments: List<RouteSegment>,
    private val colorFor: (RouteSegment) -> Int,
    private val onSegmentTapped: (RouteSegment) -> Unit,
    private val onMapTapped: () -> Unit
) : Overlay() {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val startPt = Point()
    private val endPt = Point()

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val proj = mapView.projection
        segments.forEach { seg ->
            proj.toPixels(seg.start, startPt)
            proj.toPixels(seg.end, endPt)
            paint.color = colorFor(seg)
            canvas.drawLine(startPt.x.toFloat(), startPt.y.toFloat(), endPt.x.toFloat(), endPt.y.toFloat(), paint)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        val tx = e.x; val ty = e.y
        val proj = mapView.projection
        var closest: RouteSegment? = null
        var minDist = Float.MAX_VALUE

        segments.forEach { seg ->
            proj.toPixels(seg.start, startPt)
            proj.toPixels(seg.end, endPt)
            val d = pointToSegmentDist(tx, ty, startPt.x.toFloat(), startPt.y.toFloat(), endPt.x.toFloat(), endPt.y.toFloat())
            if (d < minDist && d < TAP_TOLERANCE_PX) { minDist = d; closest = seg }
        }

        return if (closest != null) { onSegmentTapped(closest!!); true }
        else { onMapTapped(); false }
    }

    private fun pointToSegmentDist(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = bx - ax; val dy = by - ay
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) return kotlin.math.hypot(px - ax, py - ay)
        val t = ((px - ax) * dx + (py - ay) * dy) / lenSq
        val cx = ax + t.coerceIn(0f, 1f) * dx; val cy = ay + t.coerceIn(0f, 1f) * dy
        return kotlin.math.hypot(px - cx, py - cy)
    }

    companion object { private const val TAP_TOLERANCE_PX = 40f }
}

// ── Colori ────────────────────────────────────────────────────────────────────

private fun speedToAndroidColor(kmh: Float): Int = when {
    kmh < 40f  -> android.graphics.Color.rgb(76, 175, 80)
    kmh < 80f  -> android.graphics.Color.rgb(255, 200, 0)
    kmh < 120f -> android.graphics.Color.rgb(255, 152, 0)
    else       -> android.graphics.Color.rgb(244, 67, 54)
}

private fun leanToAndroidColor(deg: Float): Int = when {
    deg < 15f -> android.graphics.Color.rgb(76, 175, 80)
    deg < 30f -> android.graphics.Color.rgb(255, 200, 0)
    deg < 45f -> android.graphics.Color.rgb(255, 152, 0)
    else      -> android.graphics.Color.rgb(244, 67, 54)
}

private fun speedToComposeColor(kmh: Float): Color = when {
    kmh < 40f  -> Color(0xFF4CAF50)
    kmh < 80f  -> Color(0xFFFFC800)
    kmh < 120f -> Color(0xFFFF9800)
    else       -> Color(0xFFF44336)
}

private fun leanToComposeColor(deg: Float): Color = when {
    deg < 15f -> Color(0xFF4CAF50)
    deg < 30f -> Color(0xFFFFC800)
    deg < 45f -> Color(0xFFFF9800)
    else      -> Color(0xFFF44336)
}

// ── UI components ─────────────────────────────────────────────────────────────

@Composable
private fun SegmentTooltip(seg: RouteSegment, mapMode: MapMode, modifier: Modifier = Modifier) {
    val bgColor = if (mapMode == MapMode.SPEED) speedToComposeColor(seg.speedKmh)
                  else leanToComposeColor(seg.leanAngleDeg)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Speed, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("${seg.speedKmh.toInt()} km/h", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("⟳", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text("${seg.leanAngleDeg.toInt()}°", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun MapLegend(mapMode: MapMode, modifier: Modifier = Modifier) {
    val items = if (mapMode == MapMode.SPEED)
        listOf(Color(0xFF4CAF50) to "< 40", Color(0xFFFFC800) to "40–80", Color(0xFFFF9800) to "80–120", Color(0xFFF44336) to "> 120")
    else
        listOf(Color(0xFF4CAF50) to "< 15°", Color(0xFFFFC800) to "15–30°", Color(0xFFFF9800) to "30–45°", Color(0xFFF44336) to "> 45°")
    val unit = if (mapMode == MapMode.SPEED) "km/h" else ""

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if (mapMode == MapMode.SPEED) "Velocità:" else "Inclinazione:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        items.forEach { (color, label) -> LegendChip(color, label) }
        if (unit.isNotEmpty()) Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun LeanStatBlock(maxLeanAngleDeg: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "⟳",
            fontSize = 22.sp,
            color = leanToComposeColor(maxLeanAngleDeg)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${maxLeanAngleDeg.toInt()}°",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text("Inclinazione max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun StatBlock(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
