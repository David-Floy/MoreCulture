package com.example.moreculture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.ActivityPlaceDetailBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PlaceDetailActivity : AppCompatActivity()  {


    private  var binding: ActivityPlaceDetailBinding? = null

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    lateinit var place : Place

    private var distance : Double = 0.0

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventListRecyclerViewAdapter

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    // Place attributes
    var latitude : Double = 0.0
    var longitude :Double = 0.0
    var geoPoint : GeoPoint = GeoPoint(52.5200, 13.4050)
    var center : GeoPoint = GeoPoint(52.5200, 13.4050)
    var map : MapView? = null

    lateinit var marker : Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // setup RecyclerView
        recyclerView =
            findViewById(R.id.recyclerViewLocation)

        recyclerView.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventListRecyclerViewAdapter(this)
        recyclerView.adapter = eventAdapter


        binding?.backHomeButton?.setOnClickListener {
            finish()
        }
        val userPositionString = intent.getStringExtra("USER_POSITION")
        geoPoint = GeoPoint(userPositionString!!.split(",")[0].toDouble(), userPositionString.split(",")[1].toDouble())

        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
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
            /*val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }*/
            setUpPageDetails()
        }



    }


    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val latitude = location.latitude
            val longitude = location.longitude
            geoPoint = GeoPoint(latitude, longitude)
            Log.d("GPS", "this is my location $geoPoint")

            locationManager.removeUpdates(this)

            distance = GeoUtility().calculateDistance(geoPoint, toGeoPoint(place.geoPoint)!!)
            searchDb()
            binding?.placeDistanceDetail?.text = String.format(
                "%.0f km",
                distance
            )
        }
    }


    // Convert a string to a GeoPoint
    private fun toGeoPoint(value: String?): GeoPoint? {
        return value?.let {
            val (lat, lng) = it.split(",")
            GeoPoint(lat.toDouble(), lng.toDouble())
        }
    }

    // Setup Page UI
    private fun setUpPageDetails(){
        val placeName = intent.getStringExtra("PLACE_NAME")
        lifecycleScope.launch(Dispatchers.IO) {
           place = mainViewModel.getPlaceByName(placeName!!)
            withContext(Dispatchers.Main) {

                binding?.locationNameText?.text = place.location_name

                distance = GeoUtility().calculateDistance(geoPoint, toGeoPoint(place.geoPoint)!!)
                searchDb()
                binding?.placeDistanceDetail?.text = String.format(
                    "%.0f km",
                    distance
                )

                binding?.placeDescription?.text = place.location_description
                binding?.placeUrl?.text = place.url

                map = binding?.placeMapViewDetail
                map?.setUseDataConnection(true)
                map?.setTileSource(TileSourceFactory.MAPNIK)
                map?.setMultiTouchControls(true)
                val mapController: IMapController = map?.controller!!
                mapController.animateTo(toGeoPoint(place.geoPoint))
                addMarker(toGeoPoint(place.geoPoint))
                map?.controller?.setZoom(13)
                marker.title = place.location_name
            }

        }

        }

    private fun addMarker(center: GeoPoint?) {
        marker = Marker(map)
        marker.setPosition(center)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map?.overlays?.clear()
        map?.overlays?.add(marker)
        map?.invalidate()
    }

    private  fun searchDb() {
        lifecycleScope.launch(Dispatchers.IO) {

            val userRadius = mainViewModel.getUserRadius()
            val placeData =
                mutableMapOf<Int, Triple<Double, String, Int>>() // Store place data

            // Fetch places and events based on the selected mode

            val allEvents = mainViewModel.getEventForPlace(place.id).first()
            allEvents.forEach { event -> placeData[event.event_id] = Triple(distance, place.location_name, place.id) }


            withContext(Dispatchers.Main) {
                eventAdapter.setEvents(allEvents) // Update adapter with all events
                // Update adapter with place data
                eventAdapter.setEventPlaceData(placeData)
            }

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
                setUpPageDetails()
            }
        } else {
            // Permission denied
            setUpPageDetails()
            Toast.makeText(applicationContext, "Die App benötigt GPS-Berechtigung", Toast.LENGTH_LONG).show()

        }
    }


}