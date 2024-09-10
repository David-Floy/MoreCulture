package com.example.moreculture

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.example.MoreCulture.R


/*class EventListRecyclerViewAdapter (private val context: Context) :
    RecyclerView.Adapter<EventListRecyclerViewAdapter.MyViewHolder>() {

   /* private var events: List<EventWithTagsAndPlace> = emptyList()
    private var onEventClickListener: ((EventWithTagsAndPlace) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_list_item, parent, false) // Use your item layout
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentEvent = events[position]
        // Populate your item view with data from currentEvent
        holder.itemView.setOnClickListener {
            onEventClickListener?.invoke(currentEvent)
        }
    }

    override fun getItemCount(): Int {
        return events.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setEvents(events: List<EventWithTagsAndPlace>) {
        this.events = events
        notifyDataSetChanged()
    }

    fun setOnEventClickListener(listener: (EventWithTagsAndPlace) -> Unit) {
        onEventClickListener = listener
    }

}*/