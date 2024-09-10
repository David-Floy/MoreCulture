package com.example.moreculture.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val tag_id: Int = 0,
    @ColumnInfo(name = "tag_name") val tagName: String
)

@Entity(
    tableName = "event_tags",
    primaryKeys = ["event_id", "tag_id"],
    foreignKeys = [
        ForeignKey(entity = Event::class, parentColumns = ["event_id"], childColumns = ["event_id"]),
        ForeignKey(entity = Tag::class, parentColumns = ["tag_id"], childColumns = ["tag_id"])
    ]
)

data class EventTagCrossRef(
    val event_id: Int,
    val tag_id: Int
)

data class EventWithTags(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "tag_id",
        associateBy = Junction(EventTagCrossRef::class)
    )
    val tags: List<Tag>


)