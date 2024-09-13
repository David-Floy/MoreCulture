package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.TutorialActivityUserCreationBinding
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TutorialUserCreationActivity : AppCompatActivity() {

    private var binding: TutorialActivityUserCreationBinding? = null

    private var deselectedTagColor : Int = 0
    private var selectedTagColor : Int = 0

    private lateinit var userSelectedTags: MutableList<Int>
    private lateinit var  userAccount : UserAccount

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TutorialActivityUserCreationBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.backButton?.setOnClickListener {
            val intent = Intent(this, TutorialStartActivity::class.java)
            startActivity(intent)
        }

        setUpPageDetails()

        deselectedTagColor = ContextCompat.getColor(this, R.color.deselectedTag)
        selectedTagColor = ContextCompat.getColor(this, R.color.selectedTag)

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
                if (userSelectedTags.contains(i)) {
                    // Tag is already selected, deselect it
                    userSelectedTags.remove(i)
                    tagView.setBackgroundColor(deselectedTagColor) // Or your default color
                } else {
                    // Tag is not selected, select it
                    userSelectedTags.add(i)
                    tagView.setBackgroundColor(selectedTagColor)
                }
            }
        }

        binding?.nextButton?.setOnClickListener {
            userAccount = UserAccount(1, "", 0.0)
            userAccount.user_name = binding?.userNameInput?.text.toString()
            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.insertUserAccount(userAccount)
                mainViewModel.updateUserTags(1, userSelectedTags)

            }
            Log.d("userSelectedTags", userSelectedTags.toString())

            val intent = Intent(this, TutorialUserMapActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setUpPageDetails(){
        lifecycleScope.launch(Dispatchers.IO) {

            userSelectedTags = mainViewModel.getAllUserTags(1).toMutableList()

            withContext(Dispatchers.Main) {
                binding?.userNameInput?.tooltipText = "Hier soll dein Name stehen"
               updateTagBackgrounds(userSelectedTags)
            }
        }
    }

    private fun updateTagBackgrounds(selectedTags: MutableList<Int>){

        val deselectedTagColor  = ContextCompat.getColor(this, R.color.deselectedTag)
        val selectedTagColor = ContextCompat.getColor(this, R.color.selectedTag)


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
                if (selectedTags.contains(i)) {
                    tagView.setBackgroundColor(selectedTagColor)
                } else {
                    tagView.setBackgroundColor(deselectedTagColor)
                }
            }
        }
    }
}