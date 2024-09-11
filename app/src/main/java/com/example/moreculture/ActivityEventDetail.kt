package com.example.moreculture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.ActivityEventDetailBinding


class ActivityEventDetail  : AppCompatActivity() {


    private  var binding: ActivityEventDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.backHomeButton?.setOnClickListener {
                finish()
        }
    }
}