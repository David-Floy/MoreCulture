package com.example.moreculture.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// User Entity
@Entity(tableName = "users")
data class UserAccount (
    @PrimaryKey(autoGenerate = true) var user_id: Int = 0,
    var user_name: String,
    var user_mapRadius: Double

)

