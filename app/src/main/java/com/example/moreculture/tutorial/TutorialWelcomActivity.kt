package com.example.moreculture.tutorial

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.databinding.TutorialActivityHomeBinding
import com.example.MoreCulture.databinding.TutorialActivityWelcomeBinding
import com.example.moreculture.MainActivity
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TutorialWelcomActivity : AppCompatActivity() {

    private val mainViewModel : MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    private var welcomeBinding: TutorialActivityWelcomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("myPrefs", MODE_PRIVATE)


        welcomeBinding = TutorialActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(welcomeBinding?.root)

        welcomeBinding?.nextButton?.setOnClickListener{
            val intent = Intent(this, TutorialHomeActivity::class.java)
            startActivity(intent)
        }
        welcomeBinding?.skipButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val editor = prefs.edit()
            editor.putBoolean("hasSeenTutorial", true)
            editor.apply()
            startActivity(intent)
            finish()
        }
        var userName :String =""
        lifecycleScope.launch(Dispatchers.IO) {
            userName = mainViewModel.getUserAccount(1).user_name
            withContext(Dispatchers.Main) {
                welcomeBinding?.welcomeText?.text = "Hallo $userName, das hier ist die Startseite unserer App. Über die untere Navigationsleiste kannst du alle wichtigen Funktionen der App erreichen."
            }
        }



    }
}