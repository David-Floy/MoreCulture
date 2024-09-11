package com.example.moreculture.db


import android.database.sqlite.SQLiteConstraintException
import android.util.Log

import com.example.moreculture.GeoPointConverter
import com.example.moreculture.PlaceAlreadyExistsException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.util.GeoPoint

class MainRepository (private val database: AppDatabase, private val placeDao: PlaceDao, private val eventDao: EventDao, private val userDao: UserDao) {
    val db: AppDatabase = database



    // Places
    suspend fun insertPlace(place: Place, geoPoint: GeoPoint) :Long {
        //try {
            place.geoPoint = GeoPointConverter().fromGeoPoint(geoPoint)
            //placeDao.insertPlace(place)
            Log.d("MainActivity", "Test data added to the database")
            return placeDao.insertPlace(place)

       /*} catch (e: SQLiteConstraintException) {
            // Handle the exception, e.g., show an error message to the user
            Log.d("InsertPlace", "Place already exists")
            throw PlaceAlreadyExistsException()
        }*/
    }

    fun getPlaceIdsAndGeoPoints(): Flow<List<PlaceIdAndGeoPoint>> = placeDao.getPlaceIdsAndGeoPoints()

    fun getPlaceNameById(placeId: Int): String? {
        return placeDao.getPlaceNameById(placeId)
    }

    fun getFirstPlaceName(): String? {
        return db.PlaceDao().getFirstPlaceName()
    }

    fun getPlacesLiveData(): Flow<List<Place>> {
        return placeDao.getPlaces()
    }




    //Events
    fun getEventsForPlaceWithTags(placeId: Int, selectedTagIds: List<Int>): Flow<List<Event>> {
        return eventDao.getEventsForPlaceWithTags(placeId, selectedTagIds)
    }

    suspend fun insertEventWithTags(event: Event, tags: List<Int>) {
        //try {
            eventDao.insertEventWithTags(event, tags)
        /*} catch (e: SQLiteConstraintException){
            // Handle the exception, e.g., show an error message to the user
            Log.d("InsertEvent", "Event already exists")

            throw PlaceAlreadyExistsException()

        }*/
    }

    suspend fun insertTags(tags: List<Tag>): List<Long> {
        return eventDao.insertTags(tags)
    }

    fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }
    fun getEventForPlace (placeId: Int): Flow<List<Event>> {
        return eventDao.getEventsForPlace(placeId)
    }


    // User
    suspend fun insertUser(userAccount: UserAccount){
        return userDao.insertUser(userAccount)
    }

    suspend fun updateUserTags(userId : Int, tagIds: List<Int>){
        return userDao.updateTagIds(userId, tagIds)
    }

    fun getAllUserTags(userId: Int): List<Int> {
        return userDao.getTagIdsForUser(userId)
    }

    fun getUserRadius(): Double {
        return userDao.getUserRadius()
    }
    suspend fun updateUserRadius(userMapRadius: Double) {

        return userDao.updateUserRadius(userMapRadius)

    }


}