package com.example.moreculture.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaceDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlace(place: Place): Long

    @Query("SELECT id, geoPoint FROM places")
    fun getPlaceIdsAndGeoPoints(): Flow<List<PlaceIdAndGeoPoint>>

    @Transaction
    @Query("SELECT * FROM places")
    fun getPlaces(): Flow<List<Place>>



    @Query("SELECT location_name FROM places LIMIT 1")
     fun getFirstPlaceName(): String?

    @Query("SELECT location_name FROM places WHERE id = :placeId")
    fun getPlaceNameById(placeId: Int): String?

}

