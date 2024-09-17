package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.TutorialActivityEventlistBinding
import com.example.moreculture.MainActivity

class TutorialEventlistActivity : AppCompatActivity() {

    private var binding: TutorialActivityEventlistBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("myPrefs", MODE_PRIVATE)

        binding = TutorialActivityEventlistBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.backButton?.setOnClickListener {
            val intent = Intent(this, TutorialHomeActivity::class.java)
            startActivity(intent)

        }

        binding?.nextButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val editor = prefs.edit()
            editor.putBoolean("hasSeenTutorial", true)
            editor.apply()
            startActivity(intent)
            finish()
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