package com.example.moreculture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.ActivityMainBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.PopulateDB
import com.example.moreculture.tutorial.TutorialStartActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    // Location permission request code
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }
    private var userGeoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            checkTutorialAndGpsAccess()
        }else {
            checkTutorialAndGpsAccess()
        }
    }
    private fun checkTutorialAndGpsAccess(){

        // Überprüfen, ob das Tutorial schon angezeigt wurde
        val prefs = getSharedPreferences("myPrefs", MODE_PRIVATE)
        val hasSeenTutorial = prefs.getBoolean("hasSeenTutorial", false)

        if (!hasSeenTutorial) {
            // Wenn das Tutorial noch nicht angezeigt wurde, öffne es
            startActivity(Intent(this, TutorialStartActivity::class.java))

            finish()

        } else {
            // Wenn das Tutorial schon angezeigt wurde, öffne die Hauptanwendung
            // View Binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding?.root)



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
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                }
                setupUi()
            }
        }
    }
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val latitude = location.latitude
            val longitude = location.longitude
            userGeoPoint = GeoPoint(latitude, longitude)

            // Optionally remove updates after receiving the first location
            locationManager.removeUpdates(this)
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


        // BottomIsland Buttons
        binding?.homeButton?.setOnClickListener {
            val intent = Intent(this, TutorialStartActivity::class.java)
            startActivity(intent)
        }

        binding?.eventListButton?.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }

        binding?.accountButton?.setOnClickListener {
            val intent = Intent(this, AccountEditActivity::class.java)
            startActivity(intent)
        }
        binding?.MapButton?.setOnClickListener {
            val intent = Intent(this, MapOfPlacesActivity::class.java)
            startActivity(intent)
        }

        binding?.IWantMoreCulture?.setOnClickListener {
            val intent = Intent(this, EventDetailActivity::class.java)
            var selectedEvents : Event
            lifecycleScope.launch(Dispatchers.IO) {
                selectedEvents = mainViewModel.getRandomEvent()!!
                val place = mainViewModel.getPlaceById(selectedEvents.place_id)
                withContext(Dispatchers.Main) {

                    intent.putExtra(
                        "EVENT_ID", selectedEvents.event_id
                        )
                    intent.putExtra("EVENT_DISTANCE", GeoUtility().calculateDistance(userGeoPoint, GeoPoint(place.latitude, place.longitude)))
                    intent.putExtra("EVENT_PLACE", place.location_name)
                    startActivity(intent)
                }
            }



        }

        // Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation Drawer for Admin login and information
        navView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_option1 -> {
                    val intent = Intent(this, MenuAddPlaceActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_option2 -> {
                    val intent = Intent(this, MenuAddEventMapActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_option3 -> {
                    val intent = Intent(this, MenuTechnikActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        val burgerButton = binding?.burgerMenuButton
        burgerButton?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)

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