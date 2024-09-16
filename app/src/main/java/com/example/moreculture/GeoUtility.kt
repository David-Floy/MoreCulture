package com.example.moreculture

import org.osmdroid.util.GeoPoint
import kotlin.math.sqrt

class GeoUtility {

    // Calculate distance between two GeoPoints
    fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val lat1 = point1.latitude
        val lon1 = point1.longitude
        val lat2 = point2.latitude
        val lon2 = point2.longitude

        val dx = 71.5 * (lon1 - lon2)
        val dy = 111.3 * (lat1 - lat2)

        return sqrt(dx * dx + dy * dy)
    }

}