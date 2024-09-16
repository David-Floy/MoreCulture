package com.example.moreculture


import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
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
import java.net.HttpURLConnection
import java.net.URL

class EventDetailActivity : AppCompatActivity() {

    private var binding: ActivityEventDetailBinding? = null

    // Event attributes
    lateinit var event: Event
    private lateinit var eventTags: MutableList<Int>

    // Tag colors
    private var deselectedTagColor: Int = 0
    private var selectedTagColor: Int = 0

    // Place attributes
    var geoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)
    var center: GeoPoint = GeoPoint(52.5200, 13.4050)
    var map: MapView? = null

    lateinit var marker: Marker

    // View Model
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Initialize colors
        deselectedTagColor =
            ContextCompat.getColor(this, com.example.MoreCulture.R.color.deselectedTag)
        selectedTagColor = ContextCompat.getColor(this, com.example.MoreCulture.R.color.selectedTag)

        // Set up page details
        setUpPageDetails()

        // Set up back button click listener
        binding?.backHomeButton?.setOnClickListener {
            finish()
        }
        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }

    // Fetch event, place and selected tags
    private fun setUpPageDetails() {

        // Get event ID from intent
        val eventId = intent.getIntExtra("EVENT_ID", 0)

        // Fetch event, place and selected tags
        lifecycleScope.launch(Dispatchers.IO) {
            event = mainViewModel.getEventById(eventId)
            val place = mainViewModel.getPlaceById(event.place_id)
            eventTags = mainViewModel.getTagIdsForEvent(eventId).toMutableList()

            // Update UI with event details
            withContext(Dispatchers.Main) {
                binding?.eventNameText?.text = event.event_name
                binding?.eventDistanceDetail?.text = intent.getStringExtra("EVENT_DISTANCE")
                binding?.eventLocationDetail?.text = intent.getStringExtra("EVENT_PLACE")
                binding?.eventDate?.text = event.event_date
                binding?.eventTime?.text = event.event_time
                binding?.eventDescription?.text = event.event_description
                binding?.eventPrice?.text = event.event_price.toString()

                // WebView settings
                val viewSettings = binding?.ImageWebVIew?.settings
                viewSettings?.loadWithOverviewMode = true
                viewSettings?.useWideViewPort = true

                // Set image url
                if (event.image_url == "") {
                    binding?.ImageWebVIew?.loadUrl("https://img.zeit.de/kultur/2021-06/theater-pandemie-zuschauer-kultur-oeffnung-teaser/wide__1300x731")
                } else {
                    binding?.ImageWebVIew?.loadUrl(event.image_url!!)
                    // Check if image is available
                    Thread {
                        val isAvailable = isUrlAvailable(event.image_url!!) // No need for !! here
                        runOnUiThread {
                            if (isAvailable) {
                                binding?.ImageWebVIew?.loadUrl(event.image_url!!)
                            } else {
                                binding?.ImageWebVIew?.loadUrl("https://img.zeit.de/kultur/2021-06/theater-pandemie-zuschauer-kultur-oeffnung-teaser/wide__1300x731")
                            }
                        }
                    }.start()
                }

                // Update Tag background
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

    // Update Tag background
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

    private fun addMarker(center: GeoPoint?) {
        marker = Marker(map)
        marker.setPosition(center)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map?.overlays?.clear()
        map?.overlays?.add(marker)
        map?.invalidate()
    }

    private fun isUrlAvailable(url: String): Boolean {
        return try {
            Log.d("url Test", url)
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 3000
            connection.useCaches = false // Disable caching
            connection.instanceFollowRedirects = true // Follow redirects

            val responseCode = connection.responseCode
            val isAvailable = responseCode == HttpURLConnection.HTTP_OK ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP
            Log.d(
                "URL Availability",
                "URL: $url, Available: $isAvailable, Response Code: $responseCode"
            )
            isAvailable
        } catch (e: Exception) {
            Log.d("URL Availability", "URL: $url, Error: ${e::class.simpleName}: ${e.message}")
            false
        }
    }
}