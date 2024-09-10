package com.example.moreculture

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope

import com.example.MoreCulture.R


import com.example.MoreCulture.databinding.ActivityMainBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication

import com.example.moreculture.db.Place
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.PlaceViewModelFactory
import com.example.moreculture.db.Tag

import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null


    private val mainViewModel : MainViewModel by viewModels {
        PlaceViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Navigation Drawer Setup
        val navView = binding?.navView // Ersetze mit deiner NavigationView ID
        val drawerLayout: DrawerLayout? = binding?.drawerLayout
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout?.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Buttons
        val burgerButton = binding?.burgerMenuButton
        burgerButton?.setOnClickListener {
            //drawerLayout?.openDrawer(GravityCompat.START)

        }

        // BottomIsland Buttons
        binding?.homeButton?.setOnClickListener {

        }

        binding?.eventListButton?.setOnClickListener {
            //val intent = Intent(this, EventListActivity::class.java)
            //startActivity(intent)
        }

        binding?.accountButton?.setOnClickListener {
            // Add test Data
            lifecycleScope.launch {
                populateDatabaseWithTestData(mainViewModel)

                // Do something with the firstPlaceName (e.g., display it in a TextView)

            }
            //val firstPlaceName = mainViewModel.getFirstPlaceName()
            //Log.d("MainActivity", "First Place Name: $firstPlaceName")
        }

        // Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation Drawer for Admin login and information
        navView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_option1 -> {
                    // Handle option 1 click
                    true
                }
                R.id.nav_option2 -> {
                    // Handle option 2 click
                    true
                }
                R.id.nav_option3 -> {
                    // Handle option 3 click
                    true
                }
                else -> false
            }
        }


         // add test data
        /*lifecycleScope.launch {
            populateDatabaseWithTestData(placeViewModel, eventViewModel)
            Log.d("MainActivity", "Test data added to the database")
        }*/



    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


   // Test data

    fun createTestPlaces(): List<Place> {
        return listOf(
            Place(0, "Cool Hofen", "Green park with playground", 48.123, 11.456, "geopoint_data_1", "www.example.com/park"),
            Place(0, "Schönhausen", "Art and history museum", 48.234, 11.567, "geopoint_data_2", "www.example.com/museum"),
            Place(0, "TheaterHamburg", "Venue for concerts and events", 48.345, 11.678, "geopoint_data_3", "www.example.com/concert_hall")
        )
    }



    fun createTestEvents(): List<Event> {
        return listOf(
            Event(0, 1, "Winter spaß", "Outdoor music festival", "image_url_1", "2024-06-15"),
            Event(0, 3, "Ich bin cool haus", "Modern art exhibition", "image_url_2", "2024-07-01"),
            Event(0, 2, "Raus mit dem Regen", "Live music concert", "image_url_3", "2024-08-10")
        )
    }


    fun createTestTags(): List<Tag> {
        return listOf(
            Tag(0, "Music"),
            Tag(0, "Festival"),

            Tag(0, "Art"),
            Tag(0, "Exhibition"),
            Tag(0, "Concert")
        )
    }
    val eventTags = mapOf(
        1 to listOf(1, 2), // Event 1 has tags "Music" and "Festival"
        2 to listOf(3, 4), // Event 2 has tags "Art" and "Exhibition"
        3 to listOf(1, 5)  // Event 3 has tags "Music" and "Concert"
    )

     suspend fun populateDatabaseWithTestData(mainViewModel: MainViewModel) {
        val places = createTestPlaces()
        val events = createTestEvents()
        val tags = createTestTags()

         places.forEach { place -> val geoPoint = GeoPoint(place.latitude, place.longitude)
             mainViewModel.insertPlace(place, geoPoint)
         }
         events.forEach { event ->
             val eventTagIds = eventTags[event.event_id] ?: emptyList() // Get tag IDs for the event
             val eventTagsForEvent = tags.filter { it.tag_id in eventTagIds } // Get the actual Tag objects
             mainViewModel.insertEvent(event, eventTagsForEvent) // Pass tags to insertEvent
         }


    }
}