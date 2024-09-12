package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.TutorialActivityStartBinding
import com.example.MoreCulture.databinding.TutorialActivityUserCreationBinding

class TutorialUserCreationActivity : AppCompatActivity() {

    private var binding: TutorialActivityUserCreationBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TutorialActivityUserCreationBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.backButton?.setOnClickListener {
            val intent = Intent(this, TutorialStartActivity::class.java)
            startActivity(intent)
        }
        binding?.nextButton?.setOnClickListener {
            val intent = Intent(this, TutorialStartActivity::class.java)
            startActivity(intent)
        }




    }
}