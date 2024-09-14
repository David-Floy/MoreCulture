package com.example.moreculture.db


import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MainViewModel(private val repository: MainRepository) : ViewModel() {


    //Places
    fun getPlaces(): Flow<List<Place>> {
        return repository.getPlacesLiveData()
    }

    fun getFirstPlaceName() :String? {
        return repository.getFirstPlaceName()
    }

    fun getPlaceName(placeId: Int): String? {
        return repository.getPlaceNameById(placeId)
    }

    suspend fun insertPlace(place: Place, geoPoint: GeoPoint) : Long { // Change return type to Long
        return repository.insertPlace(place, geoPoint)
    }

    fun placeIdsAndGeoPoints(): Flow<List<PlaceIdAndGeoPoint>> = repository.getPlaceIdsAndGeoPoints()

    fun getPlaceById(placeId : Int) : Place{
        return repository.getPlaceById(placeId)
    }

    fun getPlaceByName(placeName: String): Place{
        return repository.getPlaceByName(placeName)
    }


    //Events
    // Important search
    fun eventsForPlaceWithTags(placeId : Int, selectedTagIds: List<Int>): Flow<List<Event>> = repository.getEventsForPlaceWithTags(placeId, selectedTagIds)

    suspend fun insertEventWithTags(event: Event, tags: List<Int>) =
        repository.insertEventWithTags(event, tags)

    suspend fun insertTags(tags: List<Tag>): List<Long> {
        return repository.insertTags(tags)
    }

    fun getAllEvents(): Flow<List<Event>> {
        return repository.getAllEvents()
    }

    fun getEventForPlace (placeId: Int): Flow<List<Event>> {
        return repository.getEventForPlace(placeId)
    }
    fun getEventById(eventId : Int): Event{
        return repository.getEventById(eventId)
    }
    fun getTagIdsForEvent(eventId: Int): List<Int> {
        return repository.getTagIdsForEvent(eventId)
    }


    // User
    suspend fun insertUserAccount(userAccount: UserAccount){
        repository.insertUser(userAccount)
    }
    suspend fun updateUserTags(userId : Int, tagIds: List<Int>){
        repository.updateUserTags(userId, tagIds)
    }
    fun getAllUserTags(userId: Int): List<Int> {
        return repository.getAllUserTags(userId)
    }
    fun getUserRadius(): Double {
        return repository.getUserRadius()
    }
    suspend fun updateUserRadius(userMapRadius: Double) {
        repository.updateUserRadius(userMapRadius)
    }
    fun getUserAccount(userid: Int): UserAccount {
        return repository.getUserAccount(userid)
    }
    suspend fun updateUser(user: UserAccount) {
            repository.updateUser(user)

    }



}