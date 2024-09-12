package com.example.moreculture

import android.R
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var eventTags: MutableList<Int>

    private var deselectedTagColor : Int = 0
    private var selectedTagColor : Int = 0

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

        deselectedTagColor = ContextCompat.getColor(this, com.example.MoreCulture.R.color.deselectedTag)
        selectedTagColor = ContextCompat.getColor(this, com.example.MoreCulture.R.color.selectedTag)

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
            val place = mainViewModel.getPlaceById(event.place_id)
            eventTags = mainViewModel.getTagIdsForEvent(eventId).toMutableList()


            withContext(Dispatchers.Main) {
                binding?.eventNameText?.text = event.event_name
                binding?.eventDistanceDetail?.text = intent.getStringExtra("EVENT_DISTANCE")
                binding?.eventLocationDetail?.text = intent.getStringExtra("EVENT_PLACE")
                binding?.eventDate?.text = event.event_date
                binding?.eventTime?.text = event.event_time
                binding?.eventDescription?.text = event.event_description
                binding?.eventPrice?.text = event.event_price.toString()

                updateTagBackgrounds(eventTags)

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
    private fun updateTagBackgrounds(selectedTags: List<Int>) {
        val tagIds = selectedTags// Get a list of selected tag IDs

        for (i in 1..16) {
            val tagView = when (i) {
                1 -> binding?.tag1
                2 -> binding?.tag2
                3 -> binding?.tag3
                4 -> binding?.tag4
                5 -> binding?.tag5
                6 -> binding?.tag6
                7 -> binding?.tag7
                8 -> binding?.tag8
                9 -> binding?.tag9
                10 -> binding?.tag10
                11 -> binding?.tag11
                12 -> binding?.tag12
                13 -> binding?.tag13
                14 -> binding?.tag14
                15 -> binding?.tag15
                16 -> binding?.tag16
                else -> null
            }

            if (tagView != null) {
                if (tagIds.contains(i)) {
                    tagView.setBackgroundColor(selectedTagColor)
                } else {
                    tagView.setBackgroundColor(deselectedTagColor)
                }
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