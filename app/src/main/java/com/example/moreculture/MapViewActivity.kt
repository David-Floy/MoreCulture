package com.example.moreculture

import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
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



class MapViewActivity : AppCompatActivity(){

    private var binding: ActivityMapViewBinding? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1


    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    // Place attributes
    var geoPoint : GeoPoint = GeoPoint(52.5200, 13.4050)
    var center : GeoPoint = GeoPoint(52.5200, 13.4050)
    var map : MapView? = null

    lateinit var marker : Marker
    lateinit var circle : Polygon

    var userRadius : Double = 0.0

    // Default location for Marker in case GPS is not available
    private val defaultMarkerLocation = GeoPoint(52.5200, 13.4050)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        //Top Island Buttons
        binding?.mapBackHomeButton?.setOnClickListener {
            finish()
        }
        binding?.mapConfrim?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.updateUserRadius(userRadius)
                withContext(Dispatchers.Main) {
                    finish()
                }
            }

        }


        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // MapView settings
        map = binding?.mapViewControl
        map?.setUseDataConnection(true)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setMultiTouchControls(true)
        val mapController: IMapController = map?.controller!!


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
            // Get the map again
            val mGpsMyLocationProvider = GpsMyLocationProvider(this)
            val mLocationProvider = MyLocationNewOverlay(mGpsMyLocationProvider, map)
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
                    map?.overlays?.clear()
                    //map?.overlays?.add(mLocationProvider)
                    mapController.animateTo(mLocationProvider.myLocation)
                    center = GeoPoint(mLocationProvider.myLocation)


                    geoPoint = mLocationProvider.myLocation
                    addMarker(geoPoint)

                    // Add all locations as points to the map
                    addAllLocationsToMap()

                    lifecycleScope.launch(Dispatchers.IO) {
                        userRadius = mainViewModel.getUserRadius()
                        binding?.mapViewRadiusControl?.progress = (userRadius * 1000).toInt()
                    }
                    // Update the circle with the new center and radius

                    updateCircle(userRadius)
                    // Add the circle to the map
                    map?.overlays?.add(circle)

                    // Set the initial zoom level
                    mapController.setZoom(18)
                    // Or your desired zoom level
                    mLocationProvider.disableMyLocation()
                }

            }

            val seekBar = binding?.mapViewRadiusControl
            val radiusValueTextView = binding?.mapViewRadius

            seekBar?.max = 120000 // Set the maximum value of the SeekBar
            seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val radius = (progress.toFloat() / 1000) + 0.001 // Calculate radius in km
                    userRadius = radius
                    radiusValueTextView?.text = "%.0f km".format(radius)

                    updateCircle(radius)
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
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location setup
                val map = binding?.mapViewControl // Get the map again
                val mGpsMyLocationProvider = GpsMyLocationProvider(this);val mLocationProvider = MyLocationNewOverlay(mGpsMyLocationProvider, map)
                mLocationProvider.enableMyLocation()
                mLocationProvider.enableFollowLocation()
                map?.overlays?.add(mLocationProvider)

                mLocationProvider.runOnFirstFix {
                    runOnUiThread {
                        map?.overlays?.clear()
                        map?.overlays?.add(mLocationProvider)
                        map?.controller?.animateTo(mLocationProvider.myLocation)
                        map?.controller?.setZoom(18) // Or your desired zoom level
                    }
                }
            } else {
                val map = binding?.mapViewControl // Get the map again
                map?.overlays?.clear()
                addMarker(defaultMarkerLocation)
                // Permission denied, handle accordingly (e.g., show a message)
                // You might want to inform the user that location features won't be available
            }
        }
    }


    fun addAllLocationsToMap(){
        var places : List<Place> = emptyList()
        var points : List<IGeoPoint> = mutableListOf()
        lifecycleScope.launch(Dispatchers.IO) {
            places = mainViewModel.getPlaces().first()

            withContext(Dispatchers.Main) {
                places.forEach { place ->
                    points +=(
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

                sfpo.setOnClickListener(object : SimpleFastPointOverlay.OnClickListener {
                    override fun onClick(
                        points: SimpleFastPointOverlay.PointAdapter?,
                        index: Int?
                    ) {
                        val intent = Intent(this@MapViewActivity, EventDetailActivity::class.java)
                        startActivity(intent)
                        return
                    }
                })


                map?.overlays?.add(sfpo)
            }
        }
    }

    fun updateCircle(radius: Double) {
        val points = mutableListOf<GeoPoint>()
        val earthRadius = 6371.0 // Earth's radius in kilometers

        for (i in 0..360) {
            val angle = Math.toRadians(i.toDouble())
            val lat = Math.asin(
                Math.sin(Math.toRadians(center.latitude)) * Math.cos(radius / earthRadius) +
                        Math.cos(Math.toRadians(center.latitude)) * Math.sin(radius / earthRadius) * Math.cos(angle)
            )
            val lon = Math.toRadians(center.longitude) + Math.atan2(
                Math.sin(angle) * Math.sin(radius / earthRadius) * Math.cos(Math.toRadians(center.latitude)),
                Math.cos(radius / earthRadius) - Math.sin(Math.toRadians(center.latitude)) * Math.sin(lat)
            )
            points.add(GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon)))
        }
        circle.points = points
        map?.invalidate()
    }

    fun addMarker(center: GeoPoint?) {
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