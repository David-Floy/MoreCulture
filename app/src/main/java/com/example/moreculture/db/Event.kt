package com.example.moreculture.db
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity(
    tableName = "events", indices = [Index(value = ["event_name"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Place::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("place_id"),
        onDelete = ForeignKey.CASCADE // Optional: Delete events if the location is deleted
    )]
)
data class Event(
    @PrimaryKey(autoGenerate = true) val event_id: Int = 0,
    var place_id: Int,
    //var tags: List<Tag>,
    var event_name: String,
    var event_description: String,
    var image_url: String? = null,
    var event_date: String
)

data class PlaceWithEvents(
    @Embedded var place: Place,
    @Relation(
        parentColumn = "id",
        entityColumn = "place_id"
    )
    val events: List<Event>
)




