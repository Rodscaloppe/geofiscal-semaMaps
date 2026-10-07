package com.example.shared.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoCalculations {

    private const val EARTH_RADIUS_KM = 6371.0
    private const val EARTH_RADIUS_M = 6_371_000.0

    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
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
            val lat1 = Math.toRadians(vertices[i].first)
            val lat2 = Math.toRadians(vertices[j].first)
            val dLon = Math.toRadians(vertices[j].second - vertices[i].second)
            area += dLon * (2 + sin(lat1) + sin(lat2))
        }
        area = kotlin.math.abs(area * EARTH_RADIUS_M * EARTH_RADIUS_M / 2.0)
        return area / 10_000.0
    }

    fun isValidCarNumber(car: String): Boolean {
        val pattern = Regex("^[A-Z]{2}-\\d{7}-[A-F0-9]{32}$")
        return pattern.matches(car.uppercase())
    }

    fun bearingBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) bearing += 360.0
        return bearing
    }
}
