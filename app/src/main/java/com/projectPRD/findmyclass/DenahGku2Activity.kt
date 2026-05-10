package com.projectPRD.findmyclass

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class DenahGku2Activity : AppCompatActivity() {

    private lateinit var imgFloorMap: ImageView
    private lateinit var imgUserMarker: ImageView
    private lateinit var txtTitleDenah: TextView
    private lateinit var floorMapContainer: FrameLayout

    private lateinit var btnLtd: TextView
    private lateinit var btnLt1: TextView
    private lateinit var btnLt2: TextView
    private lateinit var btnLt3: TextView

    private var currentFloor = 0 // 0 = LTD

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denah_gku2)

        val floorExtra = intent.getStringExtra("FLOOR") ?: "LTD"

        imgFloorMap       = findViewById(R.id.imgFloorMap)
        imgUserMarker     = findViewById(R.id.imgUserMarker)
        txtTitleDenah     = findViewById(R.id.txtTitleDenah)
        floorMapContainer = findViewById(R.id.floorMapContainer)

        btnLtd = findViewById(R.id.btnLtd)
        btnLt1 = findViewById(R.id.btnLt1)
        btnLt2 = findViewById(R.id.btnLt2)
        btnLt3 = findViewById(R.id.btnLt3)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        imgUserMarker.visibility = View.GONE

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        btnLtd.setOnClickListener { showFloor(0) }
        btnLt1.setOnClickListener { showFloor(1) }
        btnLt2.setOnClickListener { showFloor(2) }
        btnLt3.setOnClickListener { showFloor(3) }

        // Tentukan lantai awal dari intent
        val startFloor = when (floorExtra) {
            "LTD" -> 0
            "LT1" -> 1
            "LT2" -> 2
            "LT3" -> 3
            else  -> 0
        }
        showFloor(startFloor)
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            startGpsTracking()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        stopGpsTracking()
    }

    @SuppressLint("MissingPermission")
    private fun startGpsTracking() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 500L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val userLocation = result.lastLocation ?: return
                val distanceToGku2 = distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    GKU2_LAT, GKU2_LON
                )
                if (distanceToGku2 <= NEAR_THRESHOLD_METERS) {
                    showMarkerAtCenter()
                } else {
                    imgUserMarker.visibility = View.GONE
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request, locationCallback!!, Looper.getMainLooper()
        )
    }

    private fun stopGpsTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    private fun showMarkerAtCenter() {
        floorMapContainer.post {
            val containerW = floorMapContainer.width.toFloat()
            val containerH = floorMapContainer.height.toFloat()
            val markerW    = imgUserMarker.width.toFloat()
            val markerH    = imgUserMarker.height.toFloat()
            if (containerW == 0f || containerH == 0f) return@post
            imgUserMarker.x = containerW / 2f - markerW / 2f
            imgUserMarker.y = containerH / 2f - markerH / 2f
            imgUserMarker.visibility = View.VISIBLE
        }
    }

    private fun distanceBetween(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun showFloor(floor: Int) {
        currentFloor = floor
        txtTitleDenah.text = when (floor) {
            0    -> "GKU 2 - Lantai Dasar"
            else -> "GKU 2 - Lantai $floor"
        }
        val imageRes = when (floor) {
            0    -> R.drawable.gku2_ltd
            1    -> R.drawable.gku2_lt1
            2    -> R.drawable.gku2_lt2
            3    -> R.drawable.gku2_lt3
            else -> R.drawable.gku2_ltd
        }
        imgFloorMap.setImageResource(imageRes)
        updateFloorButtons()
    }

    private fun updateFloorButtons() {
        val buttons = listOf(0 to btnLtd, 1 to btnLt1, 2 to btnLt2, 3 to btnLt3)
        buttons.forEach { (floor, button) ->
            if (floor == currentFloor) {
                button.setBackgroundResource(R.drawable.bg_floor_selected)
                button.setTextColor(0xFF111111.toInt())
            } else {
                button.setBackgroundResource(R.drawable.bg_floor_unselected)
                button.setTextColor(0xFFFFFFFF.toInt())
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) startGpsTracking()
    }

    companion object {
        private const val GKU2_LAT              = -6.929611
        private const val GKU2_LON              = 107.768831
        private const val NEAR_THRESHOLD_METERS = 150f
        private const val REQ_LOCATION          = 202
    }
}