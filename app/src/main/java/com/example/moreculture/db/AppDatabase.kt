package com.example.moreculture.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.moreculture.GeoPointConverter


@Database(
    entities = [Place::class, Event::class, Tag::class, EventTagCrossRef::class, UserAccount::class, UserTagCrossRef::class],
    version = 5
)
@TypeConverters(GeoPointConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun PlaceDao(): PlaceDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao



    // Migration definition (usually in the same file)


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "main_db_test"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }

}