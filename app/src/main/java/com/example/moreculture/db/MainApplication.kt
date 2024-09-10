package com.example.moreculture.db

import android.app.Application
import android.content.Context

import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.osmdroid.config.Configuration



class MainApplication : Application() {

    lateinit var repository: MainRepository
        private set



    override fun onCreate() {
        super.onCreate()

        // Check and delete old database if needed (version mismatch) ---> NEEDS TO BE DELETED BEFORE NEXT RELEASE
        checkAndDeleteOldDatabase(this, "main_db", 3)


        // Configuration for MapView needed to access the map tile Server
        Configuration.getInstance().userAgentValue =
            "MentalBreakdownHappyPlaces/0.8 (Android; floy.tv.com@gmail.com)"

        // Initialize the database and repository

            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "main_db_test"
            ).build()

            val placeDao = db.PlaceDao()
            val eventDao = db.eventDao()

            repository = MainRepository(db, placeDao, eventDao)



    }


    // Function to check and delete old database if needed
    fun checkAndDeleteOldDatabase(context: Context, dbName: String, currentVersion: Int) {
        val dbFile = context.getDatabasePath(dbName)
        if (dbFile.exists()) {
            CoroutineScope(Dispatchers.IO).launch {
                val oldDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName) // Use your database class here
                    .fallbackToDestructiveMigration()
                    .build()
                try{
                    val version = oldDb.openHelper.readableDatabase.version
                    if (version != currentVersion) {
                        oldDb.close()
                        withContext(Dispatchers.Main) {
                            context.deleteDatabase(dbName)
                            println("Old database deleted due to version mismatch.")
                        }
                    }
                } finally {
                    oldDb.close()
                }
            }
        }
    }
}