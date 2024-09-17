package com.example.moreculture

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.MenuActivityKotlinJavaBinding
import com.example.MoreCulture.databinding.MenuActivityMapViewBinding

class MenuKotlinJavaActivity : AppCompatActivity() {

    private var binding: MenuActivityKotlinJavaBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = MenuActivityKotlinJavaBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.mapBackHomeButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }

}