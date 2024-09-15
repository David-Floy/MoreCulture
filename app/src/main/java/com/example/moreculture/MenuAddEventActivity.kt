package com.example.moreculture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
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

    private  var binding: MenuActivityAddEventBinding? = null
    lateinit var event : Event
    private lateinit var eventTags: List<Int>

    private var deselectedTagColor : Int = 0
    private var selectedTagColor : Int = 0

    private lateinit var userSelectedTags : MutableList<Int>

    private lateinit var intentImageUpload : Intent

    private val reQuCode = 4

    private lateinit var imageUri : Uri


    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MenuActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        deselectedTagColor = ContextCompat.getColor(this, com.example.MoreCulture.R.color.deselectedTag)
        selectedTagColor = ContextCompat.getColor(this, com.example.MoreCulture.R.color.selectedTag)

        userSelectedTags = mutableListOf()


        updateTagBackgrounds(userSelectedTags)
        event = Event(0,0, "", "", "", "", "", 0.0,)

        setUpPageDetails()

        binding?.backHomeButton?.setOnClickListener {
            finish()
        }






    }

    private fun setUpPageDetails(){
        val placeName = intent.getStringExtra("PLACE_NAME")
        binding?.eventLocationDetail?.text = placeName

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

        binding?.imageUploadView?.setOnClickListener{
            intentImageUpload = Intent(Intent.ACTION_GET_CONTENT)
            intentImageUpload.setType("image/*")
            startActivityForResult(intentImageUpload, reQuCode)
        }

        binding?.eventConfirm?.setOnClickListener{
            event.event_name = binding?.eventNameText?.text.toString()
            event.event_description = binding?.eventDistanceDetail?.text.toString()
            event.event_date = binding?.eventDate?.text.toString()
            event.event_time = binding?.eventTime?.text.toString()
            event.event_description = binding?.eventDescription?.text.toString()
            event.event_price = binding?.eventPrice?.text.toString().toDouble()


            lifecycleScope.launch (Dispatchers.IO) {
                event.place_id = mainViewModel.getPlaceByName(placeName!!).id
                mainViewModel.insertEventWithTags(event, eventTags)
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@MenuAddEventActivity, MainActivity::class.java)
                    Toast.makeText(this@MenuAddEventActivity, "Das Event wurde hinzugefügt!", Toast.LENGTH_LONG).show()
                    startActivity(intent)
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            if (requestCode == reQuCode){
                imageUri = data?.data!!
                binding?.imageUploadView!!?.setImageURI(imageUri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
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



}