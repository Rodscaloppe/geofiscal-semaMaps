package com.example.shared.platform

data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String = "",
    val severity: MarkerSeverity = MarkerSeverity.NORMAL,
    val type: MarkerType = MarkerType.INSPECTION
)

enum class MarkerSeverity { CRITICAL, HIGH, MEDIUM, NORMAL }

enum class MarkerType { DENUNCIA, INSPECTION, LIVE_GPS }

enum class MapLayer { NORMAL, SATELLITE, HYBRID, TERRAIN }

data class MapRegion(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val latitudeDelta: Double = 8.5,
    val longitudeDelta: Double = 8.0
)

interface MapController {
    fun setMapLayer(layer: MapLayer)
    fun addMarker(marker: MapMarker)
    fun removeMarker(id: String)
    fun clearMarkers()
    fun addPolyline(points: List<Pair<Double, Double>>, colorHex: Long = 0xFF2EE59D)
    fun animateToRegion(region: MapRegion, durationMs: Long = 600)
    fun fitAllMarkers(paddingPx: Int = 120)
}
