package com.example.moreculture.db

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
        SELECT e.* FROM events e
        INNER JOIN event_tags et ON e.event_id = et.event_id
        WHERE e.place_id = :placeId AND et.tag_id IN (:selectedTagIds)
        GROUP BY e.event_id
        HAVING COUNT(DISTINCT et.tag_id) = :tagCount
        """
    )
    fun getEventsForPlaceWithTags(placeId: Int, selectedTagIds: List<Int>, tagCount: Int): Flow<List<Event>>



    @Transaction
    @Query("SELECT * FROM events")
    fun getAllEventsWithTags(): Flow<List<EventWithTags>>

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>


    @Insert
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<Tag>): List<Long>

    // Fügt die Beziehungen zwischen Event und Tags in die event_tags Tabelle ein.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventTagCrossRefs(crossRefs: List<EventTagCrossRef>)

    @Transaction
    suspend fun insertEventWithTags(event: Event, tags: List<Tag>) {
        val eventId = insertEvent(event)
        val tagIds = insertTags(tags)

        val crossRefs = tagIds.mapIndexed { index, tagId ->
            EventTagCrossRef(eventId.toInt(), tagId.toInt())
        }
        insertEventTagCrossRefs(crossRefs)
    }




}

