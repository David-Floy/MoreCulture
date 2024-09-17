package com.example.moreculture

import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.databinding.ActivityMapViewBinding
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme


class MapViewActivity : AppCompatActivity() {

    private var binding: ActivityMapViewBinding? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    // View Model
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    // Place attributes
    var geoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)
    var center: GeoPoint = GeoPoint(52.5200, 13.4050)
    var map: MapView? = null


    lateinit var marker: Marker
    private lateinit var circle: Polygon

    // User radius
    var userRadius: Double = 0.0

    // Default location for Marker in case GPS is not available
    private val defaultMarkerLocation = GeoPoint(52.5200, 13.4050)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupBindings()

        // Get user radius from database
        lifecycleScope.launch(Dispatchers.IO) {
            userRadius = mainViewModel.getUserRadius()
        }


        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // Location setup
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
            mapOnCreate()
        }
    }

    // Top Island Buttons
    private fun setupBindings() {
        binding?.mapBackHomeButton?.setOnClickListener {
            finish()
        }
        binding?.mapConfrim?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.updateUserRadius(userRadius)
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@MapViewActivity, EventListActivity::class.java)
                    startActivity(intent)
                }
            }

        }
    }

    // MapView Settings
    private fun mapSettings(): IMapController {
        // MapView settings
        map = binding?.mapViewControl
        map?.setUseDataConnection(true)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setMultiTouchControls(true)
        map?.setBuiltInZoomControls(false)
        return map?.controller!!
    }

    // MapView on Create
    private fun mapOnCreate() {

        val mapController = mapSettings()
        val mGpsMyLocationProvider = GpsMyLocationProvider(this)
        val mLocationProvider = MyLocationNewOverlay(mGpsMyLocationProvider, map)
        // Enable my location
        mLocationProvider.enableMyLocation()
        mLocationProvider.enableFollowLocation()
        map?.overlays?.add(mLocationProvider)

        // Create a Polygon overlay
        circle = Polygon(map)
        circle.fillPaint.color = Color.argb(100, 255, 165, 0) // Transparent orange
        circle.outlinePaint.color = Color.TRANSPARENT

        // Run on first fix
        mLocationProvider.runOnFirstFix {
            runOnUiThread {
                // Clear the map and move the camera to the user's location
                map?.overlays?.clear()
                mapController.animateTo(mLocationProvider.myLocation)
                center = GeoPoint(mLocationProvider.myLocation)
                // Set the center of the map to the user's location
                geoPoint = mLocationProvider.myLocation
                addMarker(geoPoint)

                // Add all locations as points to the map
                addAllLocationsToMap()

                // Update the radius value in the SeekBar
                binding?.mapViewRadiusControl?.progress = (userRadius * 1000).toInt()
                Log.d("Progress Radius", binding?.mapViewRadiusControl?.progress.toString())
                Log.d("UserRadius", userRadius.toString())

                // Update the circle with the new center and radius
                updateCircle(userRadius)
                // Add the circle to the map
                map?.overlays?.add(circle)

                // Set the initial zoom level
                mapController.setZoom(18)
                // Disable my location
                mLocationProvider.disableMyLocation()
            }

        }

        // SeekBar for radius control
        val seekBar = binding?.mapViewRadiusControl
        val radiusValueTextView = binding?.mapViewRadius

        seekBar?.max = 120000 // Set the maximum value of the SeekBar

        // Set the initial progress of the SeekBar
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val radius = (progress.toFloat() / 1000) + 0.001 // Calculate radius in km
                userRadius = radius
                radiusValueTextView?.text = "%.0f km".format(radius)
                // Update the circle with the new radius
                updateCircle(radius)
                // Update the zoom level based on the radius
                val zoomLevel = when {
                    radius <= 1.0 -> 15.0
                    radius <= 5.0 -> 13.0
                    radius <= 10.0 -> 12.0
                    radius <= 20.0 -> 11.0
                    radius <= 50.0 -> 10.0
                    radius <= 100.0 -> 9.0
                    else -> 8.0
                }
                mapController.setZoom(zoomLevel)
                mapController.animateTo(center)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
                // Permission granted, proceed with location setup
                mapOnCreate()
            } else {
                // Permission denied
                val map = binding?.mapViewControl // Get the map again
                map?.overlays?.clear()
                addMarker(defaultMarkerLocation)

            }
        }
    }

    // Add all locations as points to the map
    private fun addAllLocationsToMap() {
        var places: List<Place> = emptyList()
        var points: List<IGeoPoint> = mutableListOf()

        // Get all places from the database
        lifecycleScope.launch(Dispatchers.IO) {
            places = mainViewModel.getPlaces().first()

            withContext(Dispatchers.Main) {
                places.forEach { place ->
                    points += (
                            LabelledGeoPoint(
                                place.latitude,
                                place.longitude,
                                place.location_name
                            )
                            )
                }

                val pt = SimplePointTheme(points, true)

                // create label style
                val textStyle = Paint()
                textStyle.style = Paint.Style.FILL
                textStyle.color = Color.parseColor("#0000ff")
                textStyle.textAlign = Paint.Align.CENTER
                textStyle.textSize = 40F

                val opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                    .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MEDIUM_OPTIMIZATION)
                    .setRadius(20F).setIsClickable(true).setCellSize(15)
                    .setTextStyle(textStyle)

                val sfpo = SimpleFastPointOverlay(pt, opt)

                // Set click listener for points on map
                sfpo.setOnClickListener(object : SimpleFastPointOverlay.OnClickListener {
                    override fun onClick(
                        points: SimpleFastPointOverlay.PointAdapter?,
                        index: Int?
                    ) {
                        val intent = Intent(this@MapViewActivity, PlaceDetailActivity::class.java)
                        //Log.d("PlaceName", (points?.get(index!!) as LabelledGeoPoint).getLabel())
                        intent.putExtra(
                            "PLACE_NAME",
                            (points?.get(index!!) as LabelledGeoPoint).getLabel().toString()
                        )

                        startActivity(intent)
                        return
                    }
                })
                map?.overlays?.add(sfpo)
            }
        }
    }

    // Update the circle with the new center and radius
    fun updateCircle(radius: Double) {
        val points = mutableListOf<GeoPoint>()
        val earthRadius = 6371.0 // Earth's radius in kilometers

        for (i in 0..360) {
            val angle = Math.toRadians(i.toDouble())
            val lat = Math.asin(
                Math.sin(Math.toRadians(center.latitude)) * Math.cos(radius / earthRadius) +
                        Math.cos(Math.toRadians(center.latitude)) * Math.sin(radius / earthRadius) * Math.cos(
                    angle
                )
            )
            val lon = Math.toRadians(center.longitude) + Math.atan2(
                Math.sin(angle) * Math.sin(radius / earthRadius) * Math.cos(Math.toRadians(center.latitude)),
                Math.cos(radius / earthRadius) - Math.sin(Math.toRadians(center.latitude)) * Math.sin(
                    lat
                )
            )
            points.add(GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon)))
        }
        circle.points = points
        map?.invalidate()
    }

    private fun addMarker(center: GeoPoint?) {
        marker = Marker(map)
        marker.setPosition(center)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.setIcon(getResources().getDrawable(R.drawable.ic_menu_mylocation))
        map?.overlays?.clear()
        map?.overlays?.add(marker)
        map?.invalidate()
    }

    fun toGeoPoint(value: String?): GeoPoint? {
        return value?.let {
            val (lat, lng) = it.split(",")
            GeoPoint(lat.toDouble(), lng.toDouble())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}