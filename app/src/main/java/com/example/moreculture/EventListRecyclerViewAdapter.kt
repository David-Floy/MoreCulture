package com.example.moreculture

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.example.MoreCulture.R
import com.example.moreculture.db.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count


class EventListRecyclerViewAdapter (private val context: Context) :
    RecyclerView.Adapter<EventListRecyclerViewAdapter.MyViewHolder>() {

    private var events = emptyList<Any>()
    private var onEventClickListener: ((Event) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_list_item, parent, false) // Use your item layout
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentEvent = events
        // Populate your item view with data from currentEvent
        holder.itemView.setOnClickListener {
            onEventClickListener?.invoke(currentEvent)
        }
    }

    override fun getItemCount(): Int {
        return events.count()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setEvents(events: Flow<List<Event>>) {
        this.events = events
        notifyDataSetChanged()
    }

    fun setOnEventClickListener(listener: (Event) -> Unit) {
        onEventClickListener = listener
    }

}