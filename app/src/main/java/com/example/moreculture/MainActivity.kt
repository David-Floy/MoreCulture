package com.example.moreculture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    // Location permission request code
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    // View Model
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    // User GeoPoint
    private var userGeoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the database is already populated
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDatabasePopulated = sharedPrefs.getBoolean("database_populated", false)

        // If the database is not populated, populate it
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
        } else {
            checkTutorialAndGpsAccess()
        }
    }

    // Check if the tutorial has been shown and request GPS access
    private fun checkTutorialAndGpsAccess() {

        // Check if the tutorial has been shown
        val prefs = getSharedPreferences("myPrefs", MODE_PRIVATE)
        val hasSeenTutorial = prefs.getBoolean("hasSeenTutorial", false)

        if (!hasSeenTutorial) {
            // Show the tutorial if it hasn't been shown yet
            startActivity(Intent(this, TutorialStartActivity::class.java))
            finish()

        } else {
            // Tutorial has been shown, proceed with GPS access
            // View Binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding?.root)

            // Check for location permission
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
                // Permission already granted
                // Get the last known location
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

    // Location Listener
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

    // UI Setup
    private fun setupUi() {

        // Navigation Drawer Setup
        val navView = binding?.navView // Ersetze mit deiner NavigationView ID
        val drawerLayout: DrawerLayout? = binding?.drawerLayout
        val actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout?.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()


        // BottomIsland Buttons
        binding?.homeButton?.setOnClickListener {
            val intent = Intent(this, TutorialStartActivity::class.java)
            startActivity(intent)
        }
        // BottomIsland Buttons
        binding?.eventListButton?.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }
        // BottomIsland Buttons
        binding?.accountButton?.setOnClickListener {
            val intent = Intent(this, AccountEditActivity::class.java)
            startActivity(intent)
        }

        // Map of all places Button
        binding?.MapButton?.setOnClickListener {
            val intent = Intent(this, MapOfPlacesActivity::class.java)
            startActivity(intent)
        }

        // Random Event Button
        binding?.IWantMoreCulture?.setOnClickListener {
            val intent = Intent(this, EventDetailActivity::class.java)
            var selectedEvents: Event
            lifecycleScope.launch(Dispatchers.IO) {
                selectedEvents = mainViewModel.getRandomEvent()!!
                val place = mainViewModel.getPlaceById(selectedEvents.place_id)
                withContext(Dispatchers.Main) {
                    intent.putExtra(
                        "EVENT_ID", selectedEvents.event_id
                    )
                    intent.putExtra(
                        "EVENT_DISTANCE",
                        GeoUtility().calculateDistance(
                            userGeoPoint,
                            GeoPoint(place.latitude, place.longitude)
                        )
                    )
                    intent.putExtra("EVENT_PLACE", place.location_name)
                    startActivity(intent)
                }
            }
        }

        // Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation Drawer for Admin stuff and information
        navView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // Add place
                R.id.nav_option1 -> {
                    val intent = Intent(this, MenuAddPlaceActivity::class.java)
                    startActivity(intent)
                    true
                }
                // Add event
                R.id.nav_option2 -> {
                    val intent = Intent(this, MenuAddEventMapActivity::class.java)
                    startActivity(intent)
                    true
                }
                // Technical Info
                R.id.nav_option3 -> {
                    val intent = Intent(this, MenuTechnikActivity::class.java)
                    startActivity(intent)
                    true
                }
                // Java Kotlin Activity
                R.id.nav_option4 -> {
                    val intent = Intent(this, MenuKotlinJavaActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        // Burger Menu
        val burgerButton = binding?.burgerMenuButton
        burgerButton?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUi()
            } else {
                // Permission denied setup Ui anywa
                setupUi()
                Toast.makeText(
                    applicationContext,
                    "Die App benötigt GPS-Berechtigung",
                    Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}