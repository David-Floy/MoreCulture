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
    // ... other functions ...

    @Query("SELECT * FROM events WHERE place_id = :placeId")
    fun getEventsForPlace(placeId: Int): Flow<List<Event>>

    @Query(
        """
        SELECT DISTINCT e.* FROM events e
    INNER JOIN event_tags et ON e.event_id = et.event_id
    WHERE e.place_id = :placeId AND et.tag_id IN (:selectedTagIds)
        """
    )
    fun getEventsForPlaceWithTags(placeId: Int, selectedTagIds: List<Int>): Flow<List<Event>>



    @Transaction
    @Query("SELECT * FROM events")
    fun getAllEventsWithTags(): Flow<List<EventWithTags>>

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE event_id = :eventId")
    fun getEventById(eventId: Int): Event

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventTagCrossRefs(crossRefs: List<EventTagCrossRef>)

    @Transaction
    suspend fun insertEventWithTags(event: Event, tagIds: List<Int>) {
        val eventId = insertEvent(event)
        val crossRefs = tagIds.map { tagId -> EventTagCrossRef(tagId, eventId.toInt()) }
        Log.d("EventIds","Event inserted with ID: $eventId")
        Log.d("TagIds","Tag inserted with ID: $tagIds")
        tagIds.forEach { tagId ->  insertEventTagCrossRefs(crossRefs)
        }
    }




}

