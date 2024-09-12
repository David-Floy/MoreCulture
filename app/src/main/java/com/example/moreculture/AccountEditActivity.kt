package com.example.moreculture

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.fragment.app.add
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.R
import com.example.MoreCulture.databinding.ActivityAccountEditBinding
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.Tag
import com.example.moreculture.db.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.remove

class AccountEditActivity : AppCompatActivity() {

    private  var binding: ActivityAccountEditBinding? = null

    private lateinit var userSelectedTags: MutableList<Int>
    private lateinit var  userAccount : UserAccount

    private var deselectedTagColor : Int = 0
    private var selectedTagColor : Int = 0
    private val tagUiUtility : TagUiUtility by lazy {
       TagUiUtility(binding, this, mainViewModel)
    }

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountEditBinding.inflate(layoutInflater)
        setContentView(binding?.root)
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

        binding?.accountConfirmButton?.setOnClickListener {
            userAccount.user_name = binding?.editAccountName?.text.toString()
            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.updateUser(userAccount)
                mainViewModel.updateUserTags(1, userSelectedTags)

            }
            Log.d("userSelectedTags", userSelectedTags.toString())
            finish()
        }

    }

    private fun setUpPageDetails(){
        lifecycleScope.launch(Dispatchers.IO) {
            userAccount = mainViewModel.getUserAccount(1)
            userSelectedTags = mainViewModel.getAllUserTags(1).toMutableList()

            withContext(Dispatchers.Main) {
                binding?.editAccountName?.setText(userAccount.user_name)
                tagUiUtility.updateTagBackgrounds(userSelectedTags)
                //updateTagBackgrounds(userSelectedTags)
            }
        }
    }

}


