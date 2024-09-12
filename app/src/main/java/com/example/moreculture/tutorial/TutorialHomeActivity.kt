package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.TutorialActivityHomeBinding
import com.example.moreculture.MainActivity


class TutorialHomeActivity : AppCompatActivity() {




    private var binding: TutorialActivityHomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("myPrefs", MODE_PRIVATE)


        binding = TutorialActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.backButton?.setOnClickListener {
            val intent = Intent(this, TutorialWelcomActivity::class.java)
            startActivity(intent)
        }

        binding?.nextButton?.setOnClickListener {
            val intent = Intent(this, TutorialHomeActivity::class.java)
            startActivity(intent)
        }
        binding?.skipButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val editor = prefs.edit()
            editor.putBoolean("hasSeenTutorial", true)
            editor.apply()
            startActivity(intent)
            finish()
        }

    }
}