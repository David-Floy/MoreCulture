package com.example.moreculture.db

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    // Get events for a specific place via id
    @Query("SELECT * FROM events WHERE place_id = :placeId")
    fun getEventsForPlace(placeId: Int): Flow<List<Event>>

    // Get events witch meet the selected tagsIds and from a specific place
    @Query(
        """
        SELECT DISTINCT e.* FROM events e
    INNER JOIN event_tags et ON e.event_id = et.event_id
    WHERE e.place_id = :placeId AND et.tag_id IN (:selectedTagIds)
        """
    )
    fun getEventsForPlaceWithTags(placeId: Int, selectedTagIds: List<Int>): Flow<List<Event>>

    // Get tagId List for a specific event
    @Query("SELECT tag_id FROM event_tags WHERE event_id = :eventId")
    fun getTagIdsForEvent(eventId: Int): List<Int>

    // Get all events with their tags
    @Transaction
    @Query("SELECT * FROM events")
    fun getAllEventsWithTags(): Flow<List<EventWithTags>>

    // Get all events
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    // Get event via id
    @Query("SELECT * FROM events WHERE event_id = :eventId")
    fun getEventById(eventId: Int): Event

    // Insert event
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: Event): Long

    // Insert tags
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>): List<Long>

    // Insert event-tag cross references
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventTagCrossRefs(crossRefs: List<EventTagCrossRef>)

    // Insert event with tagId List
    @Transaction
    suspend fun insertEventWithTags(event: Event, tagIds: List<Int>) {
        val eventId = insertEvent(event)
        val crossRefs = tagIds.map { tagId -> EventTagCrossRef(tagId, eventId.toInt()) }
        Log.d("EventIds", "Event inserted with ID: $eventId")
        Log.d("TagIds", "Tag inserted with ID: $tagIds")
        tagIds.forEach { tagId ->
            insertEventTagCrossRefs(crossRefs)
        }
    }

    // Get random event
    @Query("SELECT * FROM events ORDER BY RANDOM() LIMIT 1")
    fun getRandomEvent(): Event?


}

