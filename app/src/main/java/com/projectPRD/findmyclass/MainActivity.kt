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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.app.AlertDialog
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

    data class Destination(
        val building: String,
        val floor: String,
        val roomNode: String,
        val displayName: String
    )

    private val destinations = listOf(

        // ===== GKU 1 =====
        Destination("GKU1", "5", "K1_9679A", "K1.9679A"),
        Destination("GKU1", "5", "K1_9679B", "K1.9679B"),
        Destination("GKU1", "5", "K1_9680A", "K1.9680A"),
        Destination("GKU1", "5", "K1_9680B", "K1.9680B"),

        Destination("GKU1", "3", "K1_9675", "K1.9675"),
        Destination("GKU1", "3", "K1_9676", "K1.9676"),
        Destination("GKU1", "3", "K1_9677", "K1.9677"),
        Destination("GKU1", "3", "K1_9678", "K1.9678"),

        Destination("GKU1", "2", "STUDENT_LOUNGE", "Student Lounge"),
        Destination("GKU1", "1", "LOBBY", "Lobby GKU 1"),

        // ===== GKU 2 =====
        Destination("GKU2", "LTD", "K2_LTD", "GKU2 LTD"),
        Destination("GKU2", "LT1", "INDOMARET", "INDOMARET"),

        Destination("GKU2", "LT2", "K2_9653", "K2.9653"),
        Destination("GKU2", "LT2", "K2_9654", "K2.9654"),
        Destination("GKU2", "LT2", "K2_9655", "K2.9655"),
        Destination("GKU2", "LT2", "K2_9656", "K2.9656"),

        Destination("GKU2", "LT3", "K2_9657", "K2.9657")
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

        val floor = intent.getStringExtra("FLOOR")?.toIntOrNull() ?: 5

        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)
        val markerGku1 = findViewById<FrameLayout>(R.id.markerGku1)
        val markerGku2 = findViewById<FrameLayout>(R.id.markerGku2)

        infoCard = findViewById(R.id.infoCard)
        txtGedung = findViewById(R.id.txtGedung)
        txtDetail = findViewById(R.id.txtDetail)
        txtDistance = findViewById(R.id.txtDistance)

        val searchBar = findViewById<AutoCompleteTextView>(R.id.searchBar)
        val roomNames = destinations.map { it.displayName }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            roomNames
        )

        searchBar.setAdapter(adapter)
        searchBar.threshold = 1

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchBar.showDropDown()
            }
        }

        searchBar.setOnClickListener {
            searchBar.showDropDown()
        }

        searchBar.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()

            val selectedDestination = destinations.firstOrNull {
                it.displayName == selectedName
            }

            if (selectedDestination != null) {
                openDestination(selectedDestination)
            }
        }

        val btnMenuRoom = findViewById<TextView>(R.id.btnMenuRoom)

        searchBar.setOnEditorActionListener { _, _, _ ->
            searchDestination(searchBar.text.toString(), searchBar)
            true
        }

        btnMenuRoom.setOnClickListener {
            showDestinationList("ALL")
        }

        mapContainer.post {
            val width = mapContainer.width.toFloat()
            val height = mapContainer.height.toFloat()

            if (width == 0f || height == 0f) return@post

            markerGku1.x = width * 0.59f - markerGku1.width / 2
            markerGku1.y = height * 0.47f - markerGku1.height

            markerGku2.x = width * 0.41f - markerGku2.width / 2
            markerGku2.y = height * 0.52f - markerGku2.height
        }

        markerGku1.setOnClickListener {
            val intent = Intent(this, DenahGku1Activity::class.java)
            startActivity(intent)
        }

        markerGku2.setOnClickListener {
            val intent = Intent(this, DenahGku2Activity::class.java)
            startActivity(intent)
        }

        checkPermissionAndStart()
    }

    private fun openDestination(destination: Destination) {
        val intent = when (destination.building) {
            "GKU1" -> Intent(this, DenahGku1Activity::class.java)
            "GKU2" -> Intent(this, DenahGku2Activity::class.java)
            else -> return
        }

        intent.putExtra("FLOOR", destination.floor)
        intent.putExtra("TARGET_ROOM", destination.roomNode)
        intent.putExtra("TARGET_NAME", destination.displayName)

        startActivity(intent)
    }

    private fun searchDestination(keyword: String, searchBar: AutoCompleteTextView) {
        val query = keyword.lowercase()
            .replace(".", "")
            .replace(" ", "")

        val result = destinations.firstOrNull {
            it.displayName.lowercase()
                .replace(".", "")
                .replace(" ", "") == query
        }

        if (result != null) {
            openDestination(result)
        } else {
            searchBar.error = "Ruangan tidak ditemukan"
        }
    }

    private fun showDestinationList(building: String) {
        val filtered = if (building == "ALL") {
            destinations
        } else {
            destinations.filter { it.building == building }
        }

        val roomNames = filtered.map { it.displayName }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Destinasi")
            .setItems(roomNames) { _, which ->
                openDestination(filtered[which])
            }
            .show()
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