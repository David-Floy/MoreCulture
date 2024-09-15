package com.example.moreculture

import android.R
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.MoreCulture.databinding.ActivityMapViewBinding
import com.example.MoreCulture.databinding.MenuActivityAddPlaceBinding
import com.example.moreculture.db.MainApplication
import com.example.moreculture.db.MainViewModel
import com.example.moreculture.db.MainViewModelFactory
import com.example.moreculture.db.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MenuAddPlaceActivity : AppCompatActivity() {

    private var binding: MenuActivityAddPlaceBinding? = null

    private lateinit var placeLatitude :String
    private lateinit var placeLongitude :String

    private lateinit var placeName :String
    private lateinit var placeDescription :String
    private lateinit var placeUrl :String


    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    var geoPoint: GeoPoint = GeoPoint(52.5200, 13.4050)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = MenuActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        placeLatitude = intent.getStringExtra("PLACE_LATITUDE").toString()
        placeLongitude = intent.getStringExtra("PLACE_LONGITUDE").toString()
        placeName = intent.getStringExtra("PLACE_NAME").toString()
        placeDescription = intent.getStringExtra("PLACE_DESCRIPTION").toString()
        placeUrl = intent.getStringExtra("PLACE_URL").toString()
        // MapView settings
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding?.setOnMap?.setOnClickListener{
            placeLatitude = binding?.placeLatitude?.text.toString()
            placeLongitude = binding?.placeLongitude?.text.toString()

            val intent = Intent(this, MenuMapViewActivity::class.java)
            intent.putExtra("PLACE_LATITUDE", placeLatitude )
            intent.putExtra("PLACE_LONGITUDE", placeLongitude )
            intent.putExtra("PLACE_NAME", binding?.locationNameText?.text.toString() )
            intent.putExtra("PLACE_DESCRIPTION", binding?.placeDescription?.text.toString() )
            intent.putExtra("PLACE_URL",binding?.placeUrl?.text.toString() )

            startActivity(intent)
        }

        if (placeLatitude != "null" || placeLongitude != "null"){
            binding?.placeLatitude?.setText(placeLatitude)
            binding?.placeLongitude?.setText(placeLongitude)
        }
        if (placeUrl != "null"){
            binding?.placeUrl?.setText(placeUrl)
        }
        if (placeName != "null"){
            binding?.locationNameText?.setText(placeName)
        }
        if (placeDescription != "null"){
            binding?.placeDescription?.setText(placeDescription)
        }

        binding?.locationConfrim?.setOnClickListener{

            placeName = binding?.locationNameText?.text.toString()
            placeDescription = binding?.placeDescription?.text.toString()
            placeUrl = binding?.placeUrl?.text.toString()
            val placeLatitude = binding?.placeLatitude?.text.toString().toDouble()
            val placeLongitude = binding?.placeLongitude?.text.toString().toDouble()
            val placeGeoPoint = GeoPoint(placeLatitude, placeLongitude)

            val NewPlace = Place(0,placeName, placeDescription, placeLatitude, placeLongitude, fromGeoPoint(placeGeoPoint), placeUrl )

            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.insertPlace(NewPlace, placeGeoPoint)
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@MenuAddPlaceActivity, MainActivity::class.java)
                    Toast.makeText(this@MenuAddPlaceActivity, "Der Ort wurde erfolgreich hinzugefügt!", Toast.LENGTH_LONG).show()
                    startActivity(intent)

                }
            }
        }

    }
    private fun fromGeoPoint(geoPoint: GeoPoint?): String? {
        return geoPoint?.let { "${it.latitude},${it.longitude}" }
    }


}