package com.example.moreculture.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.GeoPoint

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place): Long

    @Query("SELECT id, geoPoint FROM places")
    fun getPlaceIdsAndGeoPoints(): Flow<List<PlaceIdAndGeoPoint>>

    @Transaction
    @Query("SELECT * FROM places")
    fun getPlacesWithEvents(): Flow<List<PlaceWithEvents>>



    @Query("SELECT location_name FROM places LIMIT 1")
     fun getFirstPlaceName(): String?


}

