package com.example.moreculture.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaceDao {

    // Insert a place with a GeoPoint
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlace(place: Place): Long

    // Get place IDs and GeoPoints
    @Query("SELECT id, geoPoint FROM places")
    fun getPlaceIdsAndGeoPoints(): Flow<List<PlaceIdAndGeoPoint>>

    // Get all places
    @Transaction
    @Query("SELECT * FROM places")
    fun getPlaces(): Flow<List<Place>>

    // Get the first place name
    @Query("SELECT location_name FROM places LIMIT 1")
     fun getFirstPlaceName(): String?

     // Get place name by ID
    @Query("SELECT location_name FROM places WHERE id = :placeId")
    fun getPlaceNameById(placeId: Int): String?

    // Get place by ID
    @Query("SELECT * FROM places WHERE id = :placeId")
    fun getPlaceById(placeId: Int): Place

    // Get place by name
    @Query("SELECT * FROM places WHERE location_name = :placeName")
    fun getPlaceByName(placeName: String): Place

}

