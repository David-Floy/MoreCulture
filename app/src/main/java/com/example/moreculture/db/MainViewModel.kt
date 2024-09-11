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

    fun getPlaces(): Flow<List<PlaceWithEvents>> {
        return repository.getPlacesLiveData()
    }

    // Important search
    fun eventsForPlaceWithTags(placeId : Int, selectedTagIds: List<Int>): Flow<List<Event>> = repository.getEventsForPlaceWithTags(placeId, selectedTagIds)


     fun getFirstPlaceName() :String? {
        return repository.getFirstPlaceName()
    }

    // Insert in to DB
    suspend fun insertPlace(place: Place, geoPoint: GeoPoint) : Long { // Change return type to Long
              return repository.insertPlace(place, geoPoint)
    }

    suspend fun insertEventWithTags(event: Event, tags: List<Int>) =
        repository.insertEventWithTags(event, tags)

    suspend fun insertTags(tags: List<Tag>): List<Long> {
        return repository.insertTags(tags)
    }

    // Important search
    fun placeIdsAndGeoPoints(): Flow<List<PlaceIdAndGeoPoint>> = repository.getPlaceIdsAndGeoPoints()
}