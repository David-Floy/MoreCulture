package com.example.moreculture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.databinding.MenuActivityAddEventBinding
import com.example.moreculture.db.Event
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MenuAddEventActivity : AppCompatActivity() {

    private var binding: MenuActivityAddEventBinding? = null

    lateinit var event: Event
    private lateinit var eventTags: List<Int>

    // Colors
    private var deselectedTagColor: Int = 0
    private var selectedTagColor: Int = 0

    private lateinit var userSelectedTags: MutableList<Int>

    // View Model
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MenuActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Background colors for tags
        deselectedTagColor =
            ContextCompat.getColor(this, com.example.MoreCulture.R.color.deselectedTag)
        selectedTagColor = ContextCompat.getColor(this, com.example.MoreCulture.R.color.selectedTag)

        userSelectedTags = mutableListOf()

        // Set initial background colors for all tags
        updateTagBackgrounds(userSelectedTags)

        event = Event(0, 0, "", "", "", "", "", 0.0)

        setUpPageDetails()

        binding?.backHomeButton?.setOnClickListener {
            finish()
        }
    }

    private fun setUpPageDetails() {
        // Get place name from intent
        val placeName = intent.getStringExtra("PLACE_NAME")
        binding?.eventLocationDetail?.text = placeName

        // Set Tag onclickListeners
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

            tagView?.setOnClickListener {
                if (userSelectedTags?.contains(i)!!) {
                    // Tag is already selected, deselect it
                    userSelectedTags?.remove(i)
                    tagView.setBackgroundColor(deselectedTagColor) // Or your default color
                } else {
                    // Tag is not selected, select it
                    userSelectedTags?.add(i)
                    tagView.setBackgroundColor(selectedTagColor)
                }
            }
        }


        // Set onclickListener for confirm button
        binding?.eventConfirm?.setOnClickListener {
            // Insert values in event object
            event.event_name = binding?.eventNameText?.text.toString()
            event.event_description = binding?.eventDistanceDetail?.text.toString()
            event.event_date = binding?.eventDate?.text.toString()
            event.event_time = binding?.eventTime?.text.toString()
            event.event_description = binding?.eventDescription?.text.toString()
            event.event_price = binding?.eventPrice?.text.toString().toDouble()
            event.image_url = binding?.imageUrl?.text.toString()

            eventTags = userSelectedTags

            lifecycleScope.launch(Dispatchers.IO) {
                // Insert event into database
                event.place_id = mainViewModel.getPlaceByName(placeName!!).id
                mainViewModel.insertEventWithTags(event, eventTags)
                withContext(Dispatchers.Main) {
                    // Return to main activity
                    val intent = Intent(this@MenuAddEventActivity, MainActivity::class.java)
                    Toast.makeText(
                        this@MenuAddEventActivity,
                        "Das Event wurde hinzugefügt!",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(intent)
                }
            }
        }
    }

    // Update the background colors of the tags based on the selected tags
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


}