package com.projectPRD.findmyclass

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var txtGedung: TextView
    private lateinit var txtDetail: TextView
    private lateinit var txtDistance: TextView
    private lateinit var infoCard: LinearLayout

    data class Gedung(
        val nama: String,
        val detail: String,
        val latitude: Double,
        val longitude: Double
    )

    private val gku1 = Gedung(
        nama = "GKU 1",
        detail = "K1.9675-K1.9680b, Student Lounge, Ditsama",
        latitude = 192.0,
        longitude = 365.5,
    )

    private val gku2 = Gedung(
        nama = "GKU 2",
        detail = "K2.9653-K2.9657, Indomaret, Kantin",
        latitude = 128.0,
        longitude = 404.5
    )

    private var currentGedung: Gedung? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)
        val markerGku1 = findViewById<FrameLayout>(R.id.markerGku1)
        val markerGku2 = findViewById<FrameLayout>(R.id.markerGku2)

        infoCard = findViewById(R.id.infoCard)
        txtGedung = findViewById(R.id.txtGedung)
        txtDetail = findViewById(R.id.txtDetail)
        txtDistance = findViewById(R.id.txtDistance)

        mapContainer.post {
            val width = mapContainer.width
            val height = mapContainer.height

            markerGku1.x = width * 0.70f - markerGku1.width / 2
            markerGku1.y = height * 0.53f - markerGku1.height

            markerGku2.x = width * 0.52f - markerGku2.width / 2
            markerGku2.y = height * 0.58f - markerGku2.height
        }

        markerGku1.setOnClickListener {
            val intent = Intent(this, DenahGku1Activity::class.java)
            startActivity(intent)
        }

        markerGku2.setOnClickListener {
            showGedung(gku2)
        }

        checkPermissionAndStart()
    }

    private fun showGedung(gedung: Gedung) {
        currentGedung = gedung

        txtGedung.text = gedung.nama
        txtDetail.text = gedung.detail
        txtDistance.text = "Menghitung jarak..."

        infoCard.visibility = View.VISIBLE
    }

    private fun checkPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val currentLocation = result.lastLocation ?: return
                val gedung = currentGedung ?: return

                val targetLocation = Location("").apply {
                    latitude = gedung.latitude
                    longitude = gedung.longitude
                }

                val distance = currentLocation.distanceTo(targetLocation)

                txtDistance.text = if (distance < 10) {
                    "Sudah sampai"
                } else {
                    "${distance.toInt()} meter lagi"
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}