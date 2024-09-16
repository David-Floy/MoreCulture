package com.example.moreculture.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

// Tag Entity
@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val tag_id: Int = 0,
    @ColumnInfo(name = "tag_name") val tagName: String
)

// Event cross-reference entity
@Entity(
    tableName = "event_tags",
    primaryKeys = ["event_id", "tag_id"],
    foreignKeys = [
        ForeignKey(entity = Event::class, parentColumns = ["event_id"], childColumns = ["event_id"]),
        ForeignKey(entity = Tag::class, parentColumns = ["tag_id"], childColumns = ["tag_id"])
    ]
)data class EventTagCrossRef(
    val tag_id: Int,
    val event_id: Int

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

// User cross-reference entity
@Entity(
    tableName = "user_tags",
    primaryKeys = ["user_id", "tag_id"],
    foreignKeys = [
        ForeignKey(entity = UserAccount::class, parentColumns = ["user_id"], childColumns = ["user_id"]),
        ForeignKey(entity = Tag::class, parentColumns = ["tag_id"], childColumns = ["tag_id"])
    ]
)
data class UserTagCrossRef(
    val tag_id: Int,
    val user_id: Int
)

data class UserWithTags(
    @Embedded val user: UserAccount,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "tag_id",
        associateBy = Junction(UserTagCrossRef::class)
    )
    val tags: List<Tag>
)