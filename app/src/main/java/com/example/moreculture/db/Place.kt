package com.example.moreculture.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


// Entity class for the places table
@Entity(tableName = "places", indices = [Index(value = ["location_name"], unique = true)])
data class Place (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var location_name: String,
    var location_description: String,
    var latitude: Double,
    var longitude: Double,
    var geoPoint: String? = null,
    var url: String? = null
)

data class PlaceIdAndGeoPoint(
    val id: Int,
    val geoPoint: String
)

