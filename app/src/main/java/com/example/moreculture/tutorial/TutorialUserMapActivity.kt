package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.TutorialActivityStartBinding
import com.example.MoreCulture.databinding.TutorialActivityUserMapBinding

import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory

class TutorialUserMapActivity : AppCompatActivity() {

    private var binding: TutorialActivityUserMapBinding? = null

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = TutorialActivityUserMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.nextButton?.setOnClickListener {
            //val intent = Intent(this, TutorialUserCreationActivity::class.java)
           // startActivity(intent)
        }


    }

}