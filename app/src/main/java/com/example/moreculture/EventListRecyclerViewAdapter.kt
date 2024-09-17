package com.example.moreculture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView
import android.widget.TextView


import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.example.MoreCulture.R
import com.example.moreculture.db.Event
import java.net.HttpURLConnection
import java.net.URL


class EventListRecyclerViewAdapter(private var activtiy: Activity, private val context: Context) :
    RecyclerView.Adapter<EventListRecyclerViewAdapter.MyViewHolder>() {

    // Data sources
    private var events: List<Event> = emptyList()
    private var placeDistance: List<Pair<Int, Double>> = emptyList()
    private var placeName: List<Pair<Int, String>> = emptyList()
    private var onEventClickListener: ((Event) -> Unit)? = null

    // ViewHolder creation
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_list_item, parent, false) // Use your item layout
        return MyViewHolder(itemView)
    }

    // Binding data to ViewHolder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentEvent = events[position]


        // Populate your item view with data from currentEvent
        holder.eventTitlelList.text = currentEvent.event_name
        val placeName = placeName.find { it.first == currentEvent.place_id }?.second.toString()
        holder.eventPlaceName.text = placeName

        Glide.with(this@EventListRecyclerViewAdapter.context)
            .load(currentEvent.image_url)
            .placeholder(R.drawable.test_picture)
            .error(R.drawable.test_picture)
           .into(holder.eventWebPictureList)


        // Set up WebView for picture
        /*val viewSettings = holder.eventWebPictureList.settings
        viewSettings.loadWithOverviewMode = true
        viewSettings.useWideViewPort = true


        // Load image from URL
        if (currentEvent.image_url == "") {
            holder.eventWebPictureList.loadUrl("https://img.zeit.de/kultur/2021-06/theater-pandemie-zuschauer-kultur-oeffnung-teaser/wide__1300x731")
            Log.d("Image", currentEvent.image_url!!)
        } else {
            holder.eventWebPictureList.loadUrl(currentEvent.image_url!!)
            // Check if the image URL is available
            Thread {
                val isAvailable = isUrlAvailable(currentEvent.image_url!!) // No need for !! here
                activtiy.runOnUiThread {
                    if (isAvailable) {
                        holder.eventWebPictureList.loadUrl(currentEvent.image_url!!)
                    } else {
                        holder.eventWebPictureList.loadUrl("https://img.zeit.de/kultur/2021-06/theater-pandemie-zuschauer-kultur-oeffnung-teaser/wide__1300x731")
                    }

                }
            }.start()
            }*/



        // Calculate event distance and format it as a string
        val eventDistance =
            placeDistance.find { it.first == currentEvent.place_id }?.second?.let { distance ->
                String.format(
                    "%.0f km",
                    distance
                ) // Round to nearest kilometer and format as string
            }

        holder.eventPlaceDistance.text = eventDistance

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, EventDetailActivity::class.java)
            intent.putExtra(
                "EVENT_ID",
                currentEvent.event_id
            )
            // Pass the event ID and place data
            intent.putExtra("EVENT_DISTANCE", eventDistance)
            intent.putExtra("EVENT_PLACE", placeName)// Pass the ID or any other necessary data
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return events.count()
    }

    // ViewHolder class for your item view
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventTitlelList: TextView = itemView.findViewById(R.id.eventTitlelList)
        val eventPlaceName: TextView = itemView.findViewById(R.id.eventLocationList)
        val eventPlaceDistance: TextView = itemView.findViewById(R.id.eventDistanceList)
        val eventWebPictureList: ImageView = itemView.findViewById(R.id.eventWebPictureList)

    }

    // Functions for updating data
    fun setEvents(eventsList: List<Event>) {
        this.events += eventsList
        Log.d("EventListActivity", "Filtered Events: $events")
        notifyDataSetChanged()
    }

    fun setEventPlaceData(placeData: MutableMap<Int, Triple<Double, String, Int>>) {
        this.placeDistance += placeData.map { (id, data) -> Pair(id, data.first) }
        this.placeName += placeData.map { (id, data) -> Pair(id, data.second) }
        notifyDataSetChanged()
    }

    fun setOnEventClickListener(listener: (Event) -> Unit) {
        onEventClickListener = listener
    }

    fun isEventListEmpty() = events.isEmpty()

    // Function to reset the event list
    fun ResetEventList() {
        this.events = emptyList()
        this.placeDistance = emptyList()
        this.placeName = emptyList()
        notifyDataSetChanged()
    }

    // Function to check if a URL is available
    private fun isUrlAvailable(url: String): Boolean {
        return try {
            Log.d("url Test", url)
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 3000
            connection.useCaches = false // Disable caching
            connection.instanceFollowRedirects = false // Follow redirects

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