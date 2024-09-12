package com.example.moreculture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope

import com.example.MoreCulture.R


import com.example.MoreCulture.databinding.ActivityMainBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication

import com.example.moreculture.db.Place
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.PopulateDB
import com.example.moreculture.db.Tag
import com.example.moreculture.db.UserAccount
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    // Location permission request code
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDatabasePopulated = sharedPrefs.getBoolean("database_populated", false)

        if (!isDatabasePopulated) {
            lifecycleScope.launch(Dispatchers.IO) {
                val populateDb = PopulateDB(applicationContext) // Pass applicationContext
                populateDb.populateDB(mainViewModel)
            }
            // Set the flag to true
                with(sharedPrefs.edit()) {
                    putBoolean("database_populated", true)
                    apply()
                }
            checkGpsAccessAndSetupUi()
        }else {
            checkGpsAccessAndSetupUi()
        }
    }
    private fun checkGpsAccessAndSetupUi(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            setupUi()
        }
    }

    private fun setupUi() {
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
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }

        binding?.accountButton?.setOnClickListener {
            val intent = Intent(this, AccountEditActivity::class.java)
            startActivity(intent)

            // Add test Data
            /*lifecycleScope.launch {
            populateDatabaseWithTestData(mainViewModel)

        }*/
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUi()
            } else {
                // Permission denied setup Ui anywa
                setupUi()
                Toast.makeText(applicationContext, "Die App benötigt GPS-Berechtigung", Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


   // Test data

    /*fun createTestPlaces(): List<Place> {
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
        return UserAccount(0,"David",0.252)
    }


    private fun createTestTags(): List<Tag> {
        return listOf(
            Tag(1, "Music"),
            Tag(2, "Festival"),
            Tag(3, "Art"),
            Tag(4, "Exhibition"),
            Tag(5, "Concert")
        )
    }


     suspend fun populateDatabaseWithTestData(mainViewModel: MainViewModel) {
        val places = createTestPlaces()
        val events = createTestEvents()
        val tags = createTestTags()
        val user = createTestUser()
         mainViewModel.insertTags(tags)

         places.forEach { place -> val geoPoint = GeoPoint(place.latitude, place.longitude)
             mainViewModel.insertPlace(place, geoPoint)
         }
         events.forEach { event ->
             val eventTagIds = eventTags[event.event_id] ?: emptyList()
             Log.d("MainActivity", "Event Tag IDs: $eventTagIds")
             mainViewModel.insertEventWithTags(event, eventTagIds)
         }
         mainViewModel.insertUserAccount(user)

         mainViewModel.updateUserTags(1, listOf(1,5))
    }*/


}