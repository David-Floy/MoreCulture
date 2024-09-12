package com.example.moreculture

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import androidx.recyclerview.widget.RecyclerView
import com.example.MoreCulture.R
import com.example.moreculture.db.Event


class EventListRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<EventListRecyclerViewAdapter.MyViewHolder>() {

    private var events: List<Event> = emptyList()
    private var placeDistance:  List<Pair<Int, Double>> = emptyList()
    private var placeName:  List<Pair<Int, String>> = emptyList()
    private var onEventClickListener: ((Event) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_list_item, parent, false) // Use your item layout
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val currentEvent = events[position]
            // Populate your item view with data from currentEvent
            holder.eventTitlelList.text = currentEvent.event_name

        var placeName = placeName.find { it.first == currentEvent.place_id }?.second.toString()
            holder.evntPlaceName.text = placeName

        var eventDistance = placeDistance.find { it.first == currentEvent.place_id }?.second?.let { distance ->
            String.format(
                "%.0f km",
                distance
            ) // Round to nearest kilometer and format as string
        }

            holder.eventPlaceDistance.text = eventDistance

            holder.itemView.setOnClickListener {
                val intent = Intent(context, EventDetailActivity::class.java)
                intent.putExtra(
                    "EVENT_ID",
                    events[position].event_id
                )
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
        val eventTitlelList : TextView = itemView.findViewById(R.id.eventTitlelList)
        val evntPlaceName : TextView = itemView.findViewById(R.id.eventLocationList)
        val eventPlaceDistance : TextView = itemView.findViewById(R.id.eventDistanceList)
    }

    fun setEvents(eventsList: List<Event>) {
        this.events += eventsList
        Log.d("EventListActivity", "Filtered Events: $events")
        notifyDataSetChanged()
    }
    fun setEventPlaceData(placeData: MutableMap<Int, Triple<Double, String, Int>> ){
        this.placeDistance += placeData.map { (id, data) -> Pair(id, data.first) }
        this.placeName += placeData.map { (id, data) -> Pair(id, data.second) }
    }

    fun setOnEventClickListener(listener: (Event) -> Unit) {
        onEventClickListener = listener
    }

    fun isEventListEmpty() = events.isEmpty()

    fun ResetEventList(){
        this.events = emptyList()
        this.placeDistance = emptyList()
        this.placeName = emptyList()
        notifyDataSetChanged()
    }



}