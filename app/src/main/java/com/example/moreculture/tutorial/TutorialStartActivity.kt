package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.ActivityMainBinding
import com.example.MoreCulture.databinding.TutorialActivityStartBinding
import com.example.moreculture.db.UserAccount

class TutorialStartActivity : AppCompatActivity() {

    private var binding: TutorialActivityStartBinding? = null

    private lateinit var userSelectedTags: MutableList<Int>
    private lateinit var  userAccount : UserAccount

    private var deselectedTagColor : Int = 0
    private var selectedTagColor : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = TutorialActivityStartBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.nextButton?.setOnClickListener {
            val intent = Intent(this, TutorialUserCreationActivity::class.java)
            startActivity(intent)
        }


    }


}