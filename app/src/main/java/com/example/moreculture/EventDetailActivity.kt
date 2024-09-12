package com.example.moreculture

import android.R
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.TypeConverter
import com.example.MoreCulture.databinding.ActivityEventDetailBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class EventDetailActivity  : AppCompatActivity() {


    private  var binding: ActivityEventDetailBinding? = null
    lateinit var event : Event

    // Place attributes
    var latitude : Double = 0.0
    var longitude :Double = 0.0
    var geoPoint : GeoPoint = GeoPoint(52.5200, 13.4050)
    var center : GeoPoint = GeoPoint(52.5200, 13.4050)
    var map : MapView? = null

    lateinit var marker : Marker

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        setUpPageDetails()

        binding?.backHomeButton?.setOnClickListener {
                finish()
        }
        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))





    }

    private fun setUpPageDetails(){
        val eventId = intent.getIntExtra("EVENT_ID", 0)
        lifecycleScope.launch(Dispatchers.IO) {
            event = mainViewModel.getEventById(eventId)
            var place = mainViewModel.getPlaceById(event.place_id)
            withContext(Dispatchers.Main) {
                binding?.eventNameText?.text = event.event_name
                binding?.eventDistanceDetail?.text = intent.getStringExtra("EVENT_DISTANCE")
                binding?.eventLocationDetail?.text = intent.getStringExtra("EVENT_PLACE")
                binding?.eventDate?.text = event.event_date
                binding?.eventTime?.text = event.event_time
                binding?.eventDescription?.text = event.event_description
                binding?.eventPrice?.text = event.event_price.toString()



                // MapView settings
                map = binding?.eventMapViewDetail
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

    @TypeConverter
    fun toGeoPoint(value: String?): GeoPoint? {
        return value?.let {
            val (lat, lng) = it.split(",")
            GeoPoint(lat.toDouble(), lng.toDouble())
        }
    }
    fun addMarker(center: GeoPoint?) {
        marker = Marker(map)
        marker.setPosition(center)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map?.overlays?.clear()
        map?.overlays?.add(marker)
        map?.invalidate()
    }
}