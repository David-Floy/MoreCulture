package com.example.moreculture

import android.os.Bundle
import android.util.Log
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.ActivityMainBinding
import com.example.moreculture.db.EventViewModel
import com.example.moreculture.db.EventViewModelFactory
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.Tag
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class EventListActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventListRecyclerViewAdapter



    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }


    private val defaultMarkerLocation = GeoPoint(52.5200, 13.4050)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event_list)


        val tagUserList = setOf<Tag>(
            Tag(1, "Music"),
            Tag(2, "Festival"),
            Tag(3, "Concert"),
            Tag(4, "Party"),
            Tag(5, "Food")
        )

         recyclerView =
            findViewById(R.id.recyclerViewEventList)

        recyclerView.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventListRecyclerViewAdapter(this)
        recyclerView.adapter = eventAdapter

       // Or any other layout manager

        lifecycleScope.launch {
            mainViewModel.placeIdsAndGeoPoints().collect { places ->
                val filteredPlaces = places.filter { (id, geoPoint) ->
                    val distance = GeoUtility().calculateDistance(defaultMarkerLocation, GeoPointConverter.toGeoPoint(it))
                    distance <= 100
                } ?: false

                val filteredEvents = mainViewModel.eventsForPlaceWithTags(filteredPlaces[0].id, tagUserList.map { it.tag_id })

                eventAdapter.setEvents(filteredEvents)
            }
        }

        eventAdapter.setOnEventClickListener { event ->
            // Handle the event click, e.g., navigate to details screen
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null

    }

}