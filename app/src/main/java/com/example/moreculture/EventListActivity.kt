package com.example.moreculture

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import org.osmdroid.util.GeoPoint

class EventListActivity : AppCompatActivity() {

    private var binding: ActivityEventListBinding? = null

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

        // Test List of Tags
        tagUserList = listOf(
            //Tag(1, "Music"),
            //Tag(2, "Festival"),
            Tag(3, "Concert"),
            Tag(4, "Party"),
            Tag(5, "Food")
        )

        recyclerView =
            findViewById(R.id.recyclerViewEventList)

        recyclerView.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventListRecyclerViewAdapter(this)
        recyclerView.adapter = eventAdapter

        // Initial search with mode 1 when the activity is created
        searchDb(1)

        // Set click listeners for the top filter buttons
        binding?.buttonFeed?.setOnClickListener {
            eventAdapter.ResetEventList()
            searchDb(1)
            CheckVisibility()
        }
        binding?.buttonAll?.setOnClickListener {
            eventAdapter.ResetEventList()
            searchDb(2)
            CheckVisibility()
        }
        binding?.buttonClose?.setOnClickListener {
            eventAdapter.ResetEventList()
            searchDb(3)
            CheckVisibility()
        }

        CheckVisibility()


    }

    // Function to check if the event list is empty and update the visibility of the empty state view
    fun CheckVisibility(){
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
    // 2: All -> all events
    // 3: Close -> all events within userRadius
    private fun searchDb(mode: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch places and events based on the selected mode
            mainViewModel.placeIdsAndGeoPoints().collect { places ->
                val filteredPlaces = places.mapNotNull { (id, geoPoint) ->
                    geoPoint?.let {
                        val distance = GeoUtility().calculateDistance(
                            defaultMarkerLocation,
                            toGeoPoint(geoPoint)!!
                        )
                        if (distance <= 1 && mode == 1) {
                            Pair(id, distance)
                        } else if (mode == 2 || mode == 3) {
                            Pair(id, distance)
                        }
                        else{
                            null
                        }
                    }
                }

                val allEvents = mutableListOf<Event>() // Accumulate events from all places
                val placeData =
                    mutableMapOf<Int, Triple<Double, String, Int>>() // Store place data

                if (mode == 3) {
                    val filteredEvents = mainViewModel.getAllEvents().first()
                    Log.d("Mode3 Event Close", "$filteredEvents")
                    filteredPlaces.forEach { (id, distance) ->
                        val placeName = mainViewModel.getPlaceName(id).toString()
                        placeData[id] = Triple(distance, placeName, id)}
                    allEvents.addAll(filteredEvents)
                } else {
                    filteredPlaces.forEach { (id, distance) ->
                        val placeName = mainViewModel.getPlaceName(id).toString()
                        placeData[id] = Triple(distance, placeName, id)

                        val filteredEvents = mainViewModel.eventsForPlaceWithTags(id, tagUserList.map { it.tag_id }).first()
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null

    }

}