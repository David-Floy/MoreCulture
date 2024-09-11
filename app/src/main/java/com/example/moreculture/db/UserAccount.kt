package com.example.moreculture.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "users")
data class UserAccount (
    @PrimaryKey(autoGenerate = true) var user_id: Int = 0,
    var user_name: String,
    var user_mapRadius: Double

)

