package com.example.moreculture.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.moreculture.GeoPointConverter

// Imports DAO classes
@Database(
    entities = [Place::class, Event::class, Tag::class, EventTagCrossRef::class, UserAccount::class, UserTagCrossRef::class],
    version = 6
)

@TypeConverters(GeoPointConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun PlaceDao(): PlaceDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "main_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }

}