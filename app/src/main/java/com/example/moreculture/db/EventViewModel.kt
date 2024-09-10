package com.example.moreculture.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class EventViewModel(private val repository: MainRepository) : ViewModel() {

    fun getEvents(): Flow<List<PlaceWithEvents>> {
        return repository.getPlacesLiveData()
    }

    /*fun getEvent(eventId: Int): Flow<EventWithTagsAndPlace> {
        return repository.getEventWithTagsAndPlace(eventId)
    }

    suspend fun insertEvent(event: Event, tags: List<Tag>) {
        viewModelScope.launch {
            repository.insertEventWithTags(event, tags)
        }
    }
    suspend fun getSortedEvents(targetTags: Set<Tag>): Flow<List<EventWithTagsAndPlace>> {
        return repository.getAllEventsWithTags().map { events ->
            events.sortedByDescending { eventWithTagsAndPlace ->
                eventWithTagsAndPlace.tags.count { it in targetTags }
            }
        }
    }*/

}