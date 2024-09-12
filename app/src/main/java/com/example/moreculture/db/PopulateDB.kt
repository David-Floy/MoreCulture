package com.example.moreculture.db

import android.content.Context
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class PopulateDB(private val applicationContext: Context){

    interface PopulateDbCallback {
        fun onDatabasePopulated()
    }

    fun populateDB(mainViewModel: MainViewModel, callback: PopulateDbCallback? = null){
        CoroutineScope(Dispatchers.IO).launch {
            val places = createTestPlaces()
            val events = createTestEvents()
            val tags = createTags()
            val user = createTestUser()
            val eventTags = mapOf(
                1 to listOf(1, 2), // Event 1 has tags "Music" and "Festival"
                2 to listOf(3, 4), // Event 2 has tags "Art" and "Exhibition"
                3 to listOf(1, 5)  // Event 3 has tags "Music" and "Concert"
            )

            mainViewModel.insertTags(tags)

            places.forEach { place ->
                val geoPoint = GeoPoint(place.latitude, place.longitude)
                mainViewModel.insertPlace(place, geoPoint)
            }
            events.forEach { event ->
                val eventTagIds = eventTags[event.event_id] ?: emptyList()
                Log.d("MainActivity", "Event Tag IDs: $eventTagIds")
                mainViewModel.insertEventWithTags(event, eventTagIds)
            }
            mainViewModel.insertUserAccount(user)

            mainViewModel.updateUserTags(1, listOf(1, 5))

        }
    }

    fun createTestPlaces(): List<Place> {
        return listOf(
            Place(0, "Cool Hofen", "Green park with playground", 52.520652, 13.406026, "geopoint_data_1", "www.example.com/park"),
            Place(0, "Schönhausen", "Art and history museum", 48.234, 11.567, "geopoint_data_2", "www.example.com/museum"),
            Place(0, "TheaterHamburg", "Venue for concerts and events", 48.345, 11.678, "geopoint_data_3", "www.example.com/concert_hall")
        )
    }



    fun createTestEvents(): List<Event> {
        return listOf(
            Event(1, 1, "Winter spaß", "Outdoor music festival", "image_url_1", "2024-06-15", "20:00", 100.00),
            Event(2, 2, "Ich bin cool haus", "Modern art exhibition", "image_url_2", "2024-07-01", "19:30", 50.00),
            Event(3, 3, "Raus mit dem Regen", "Live music concert", "image_url_3", "2024-08-10", "21:00", 75.00)
        )
    }

    fun createTestUser() : UserAccount{
        return UserAccount(0,"David",20.00)
    }


    private fun createTags(): List<Tag> {
        return listOf(
            Tag(1, "Theater"),
            Tag(2, "Concert"),
            Tag(3, "Exhibition"),
            Tag(4, "Art"),
            Tag(5, "Opera"),
            Tag(6, "Modern"),
            Tag(7, "Classic"),
            Tag(8, "OpenAir"),
            Tag(9, "Creative"),
            Tag(10, "Workshop"),
            Tag(11, "DisabledParking"),
            Tag(12, "BicycleParking"),
            Tag(13, "PublicTransport"),
            Tag(14, "Subtitle"),
            Tag(15, "Accessibility"),
            Tag(16, "CarParking")
        )
    }
}