package com.example.moreculture

import android.R
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.databinding.MenuActivityAddEventMapBinding
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

class MenuAddEventMapActivity : AppCompatActivity() {

    private var binding: MenuActivityAddEventMapBinding? = null

    // View Model
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }


    var center: GeoPoint = GeoPoint(52.5200, 13.4050)
    var map: MapView? = null
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = MenuActivityAddEventMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupBindings()
        mapOnCreate()

    }

    //Top Island Buttons
    private fun setupBindings() {
        binding?.mapBackHomeButton?.setOnClickListener {
            finish()
        }
    }

    // MapView Settings
    private fun mapSettings(): IMapController {
        // MapView settings
        map = binding?.mapViewControl
        map?.setUseDataConnection(true)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setMultiTouchControls(true)
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


        // Run on first fix
        mLocationProvider.runOnFirstFix {
            runOnUiThread {
                map?.overlays?.clear()
                //map?.overlays?.add(mLocationProvider)
                mapController.animateTo(mLocationProvider.myLocation)
                center = GeoPoint(mLocationProvider.myLocation)

                addMarker(center)

                // Add all locations as points to the map
                addAllLocationsToMap()

                // Set the initial zoom level
                mapController.setZoom(11)

                // Disable my location
                mLocationProvider.disableMyLocation()
            }

        }
    }

    // Add all locations as points to the map
    private fun addAllLocationsToMap() {
        var places: List<Place> = emptyList()
        var points: List<IGeoPoint> = mutableListOf()

        lifecycleScope.launch(Dispatchers.IO) {
            // get all places from database
            places = mainViewModel.getPlaces().first()

            withContext(Dispatchers.Main) {
                // add all places to the map
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

                // set onclick listener for points on the map
                sfpo.setOnClickListener(object : SimpleFastPointOverlay.OnClickListener {
                    override fun onClick(
                        points: SimpleFastPointOverlay.PointAdapter?,
                        index: Int?
                    ) {
                        val intent = Intent(this@MenuAddEventMapActivity, MenuAddEventActivity::class.java)
                        //Log.d("PlaceName", (points?.get(index!!) as LabelledGeoPoint).getLabel())
                        intent.putExtra("PLACE_NAME", (points?.get(index!!) as LabelledGeoPoint).getLabel())
                        startActivity(intent)
                        return
                    }
                })

                map?.overlays?.add(sfpo)
            }
        }
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
}
