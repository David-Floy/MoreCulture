package com.example.moreculture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log

import android.view.View
import android.widget.Toast

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.ActivityEventListBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

class EventListActivity : AppCompatActivity() {

    private var binding: ActivityEventListBinding? = null

    // Location permission request code and location setup
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    var userGeoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventListRecyclerViewAdapter
    var tagUserList: List<Tag> = emptyList()


    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    // Default marker location
    private val defaultMarkerLocation = GeoPoint(52.5200, 13.4050)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))


        recyclerView =
            findViewById(R.id.recyclerViewEventList)

        recyclerView.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventListRecyclerViewAdapter(this)
        recyclerView.adapter = eventAdapter

        //
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
            // Permission already granted, proceed with location setup
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

    private fun setupUi() {
        // Get the last known user location
        // Initial search with mode 1 when the activity is created
        searchDb(1)

        // Set click listeners for the top filter buttons
        binding?.buttonFeed?.setOnClickListener {

            eventAdapter.ResetEventList()
            searchDb(1)
            checkVisibility()
        }
        binding?.buttonAll?.setOnClickListener {

            eventAdapter.ResetEventList()
            searchDb(2)
            checkVisibility()

        }
        binding?.buttonClose?.setOnClickListener {
            eventAdapter.ResetEventList()
            searchDb(3)
            checkVisibility()
        }
        checkVisibility()

        // Home Button
        binding?.homeButton?.setOnClickListener {
            finish()
        }
        binding?.mapButton?.setOnClickListener {
            val intent = Intent(this, MapViewActivity::class.java)
            intent.putExtra("UserPosition", userGeoPoint.toString())
            this.startActivity(intent)
        }
    }


    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val latitude = location.latitude
            val longitude = location.longitude
            userGeoPoint = GeoPoint(latitude, longitude)
            Log.d("GPS", "this is my location $userGeoPoint")
            eventAdapter.ResetEventList()
            searchDb(1)
            checkVisibility()

            // Optionally remove updates after receiving the first location
            locationManager.removeUpdates(this)
        }
    }

    // Function to check if the event list is empty and update the visibility of the empty state view
    private fun checkVisibility() {
        lifecycleScope.launch {
            delay(1000) // Delay for 1 second (1000 milliseconds)
            withContext(Dispatchers.Main) { // Switch to the main thread for UI operations
                if (eventAdapter.isEventListEmpty()) {
                    binding?.emptyStateView?.visibility = View.VISIBLE
                } else {
                    binding?.emptyStateView?.visibility = View.INVISIBLE
                }
            }
        }
    }

    // Function to search the database based on the selected mode
    // 1: Feed -> all events within userRadius and userTagsList
    // 2: All -> all events with userTags
    // 3: Close -> all events within userRadius
    private fun searchDb(mode: Int) {
        lifecycleScope.launch(Dispatchers.IO) {

            val userRadius = mainViewModel.getUserRadius()

            // Fetch places and events based on the selected mode

            mainViewModel.placeIdsAndGeoPoints().collect { places ->
                val filteredPlaces = places.mapNotNull { (id, geoPoint) ->
                    geoPoint?.let {

                        val distance = GeoUtility().calculateDistance(
                            userGeoPoint,
                            toGeoPoint(geoPoint)!!
                        )
                        if (distance <= userRadius && (mode == 1 || mode == 3)) {
                            Pair(id, distance)
                        } else if (mode == 2) {
                            Pair(id, distance)
                        } else {
                            null
                        }
                    }
                }

                val allEvents = mutableListOf<Event>() // Accumulate events from all places
                val placeData =
                    mutableMapOf<Int, Triple<Double, String, Int>>() // Store place data

                if (mode == 3) {
                    filteredPlaces.forEach { (id, distance) ->
                        val placeName = mainViewModel.getPlaceName(id).toString()
                        placeData[id] = Triple(distance, placeName, id)
                        val filteredEvents = mainViewModel.getEventForPlace(id).first()
                        allEvents.addAll(filteredEvents)
                    }
                } else {
                    filteredPlaces.forEach { (id, distance) ->
                        val placeName = mainViewModel.getPlaceName(id).toString()
                        placeData[id] = Triple(distance, placeName, id)

                        val tagUserList = mainViewModel.getAllUserTags(1)

                        val filteredEvents =
                            mainViewModel.eventsForPlaceWithTags(id, tagUserList.map { it }).first()
                        allEvents.addAll(filteredEvents)
                    }
                }
                withContext(Dispatchers.Main) {
                    eventAdapter.setEvents(allEvents) // Update adapter with all events
                    eventAdapter.setEventPlaceData(placeData) // Update adapter with place data
                }
            }
        }
    }


    // Convert a string to a GeoPoint
    fun toGeoPoint(value: String?): GeoPoint? {
        return value?.let {
            val (lat, lng) = it.split(",")
            GeoPoint(lat.toDouble(), lng.toDouble())
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        } else {
            // Permission denied
            setupUi()
            Toast.makeText(applicationContext, "Die App benötigt GPS-Berechtigung", Toast.LENGTH_LONG).show()

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null

    }

}