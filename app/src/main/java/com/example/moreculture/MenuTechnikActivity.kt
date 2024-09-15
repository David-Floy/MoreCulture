package com.example.moreculture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.MoreCulture.databinding.MenuActivityMapViewBinding
import com.example.MoreCulture.databinding.MenuActivtiyTechnikBinding

class MenuTechnikActivity : AppCompatActivity() {

    private var binding: MenuActivtiyTechnikBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MenuActivtiyTechnikBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.mapBackHomeButton?.setOnClickListener {
            finish()
        }

    }
}