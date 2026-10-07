package com.example.shared.geo

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private fun Double.toRadians(): Double = this * PI / 180.0
private fun Double.toDegrees(): Double = this * 180.0 / PI

object GeoCalculations {

    private const val EARTH_RADIUS_KM = 6371.0
    private const val EARTH_RADIUS_M = 6_371_000.0

    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return haversineDistanceKm(lat1, lon1, lat2, lon2) * 1000.0
    }

    fun calculateTrajectoryDistanceKm(points: List<Pair<Double, Double>>): Double {
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += haversineDistanceKm(
                points[i].first, points[i].second,
                points[i + 1].first, points[i + 1].second
            )
        }
        return total
    }

    fun calculatePolygonAreaHectares(vertices: List<Pair<Double, Double>>): Double {
        if (vertices.size < 3) return 0.0
        var area = 0.0
        val n = vertices.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            val lat1 = vertices[i].first.toRadians()
            val lat2 = vertices[j].first.toRadians()
            val dLon = (vertices[j].second - vertices[i].second).toRadians()
            area += dLon * (2 + sin(lat1) + sin(lat2))
        }
        area = abs(area * EARTH_RADIUS_M * EARTH_RADIUS_M / 2.0)
        return area / 10_000.0
    }

    fun isValidCarNumber(car: String): Boolean {
        val pattern = Regex("^[A-Z]{2}-\\d{7}-[A-F0-9]{32}$")
        return pattern.matches(car.uppercase())
    }

    fun bearingBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = (lon2 - lon1).toRadians()
        val lat1Rad = lat1.toRadians()
        val lat2Rad = lat2.toRadians()
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        var bearing = atan2(y, x).toDegrees()
        if (bearing < 0) bearing += 360.0
        return bearing
    }
}
