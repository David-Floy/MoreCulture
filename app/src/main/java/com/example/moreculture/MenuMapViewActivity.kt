package com.example.moreculture

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.MenuActivityMapViewBinding
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MenuMapViewActivity : AppCompatActivity() {

    private var binding: MenuActivityMapViewBinding? = null

    // Place Details
    private lateinit var newLatitude: String
    private lateinit var newLongitude: String
    private lateinit var placeName: String
    private lateinit var placeDescription: String
    private lateinit var placeUrl: String

    var geoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)

    var map: MapView? = null
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = MenuActivityMapViewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Get place details from intent
        newLatitude = intent.getStringExtra("PLACE_LATITUDE").toString()
        newLongitude = intent.getStringExtra("PLACE_LONGITUDE").toString()
        placeName = intent.getStringExtra("PLACE_NAME").toString()
        placeDescription = intent.getStringExtra("PLACE_DESCRIPTION").toString()
        placeUrl = intent.getStringExtra("PLACE_URL").toString()

        // check if place details are empty
        if (newLatitude != "" || newLongitude != "") {
            geoPoint = GeoPoint(newLongitude?.toDouble()!!, newLatitude?.toDouble()!!)
        }
        binding?.mapConfrim?.setOnClickListener {
            // Start MenuAddPlaceActivity with place details
            val intent = Intent(this, MenuAddPlaceActivity::class.java)
            intent.putExtra("PLACE_LATITUDE", marker.position.latitude.toString())
            intent.putExtra("PLACE_LONGITUDE", marker.position.longitude.toString())
            intent.putExtra("PLACE_NAME", placeName)
            intent.putExtra("PLACE_DESCRIPTION", placeDescription)
            intent.putExtra("PLACE_URL", placeUrl)
            startActivity(intent)
        }
        mapOnCreate()
    }


    // MapView Settings
    private fun mapSettings(): IMapController {
        // MapView settings
        map = binding?.mapViewControl
        map?.setUseDataConnection(true)
        map?.setBuiltInZoomControls(false)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setMultiTouchControls(true)
        return map?.controller!!
    }

    // MapView on Create
    private fun mapOnCreate() {
        // Get the map again
        val mapController = mapSettings()
        val mGpsMyLocationProvider = GpsMyLocationProvider(this)
        val mLocationProvider = MyLocationNewOverlay(mGpsMyLocationProvider, map)
        mLocationProvider.enableMyLocation()
        mLocationProvider.enableFollowLocation()
        map?.overlays?.add(mLocationProvider)


        // Run on first fix
        mLocationProvider.runOnFirstFix {
            runOnUiThread {
                map?.overlays?.clear()

                // Set the initial location
                if (newLatitude == "" || newLongitude == "") {
                    geoPoint = mLocationProvider.myLocation
                    mapController.animateTo(mLocationProvider.myLocation)
                } else {
                    geoPoint = GeoPoint(newLatitude.toDouble(), newLongitude.toDouble())
                    mapController.animateTo(geoPoint)
                }

                addMarker(geoPoint)
                // Set the initial zoom level
                mapController.setZoom(18)
                mLocationProvider.disableMyLocation()
            }

        }


    }

    private fun addMarker(center: GeoPoint?) {
        marker = Marker(map)
        marker.setPosition(center)
        marker.isDraggable = true
        //marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map?.overlays?.clear()
        map?.overlays?.add(marker)
        map?.invalidate()
    }
}