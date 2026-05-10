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

class DenahGku1Activity : AppCompatActivity() {

    private lateinit var imgFloorMap: ImageView
    private lateinit var imgUserMarker: ImageView
    private lateinit var txtTitleDenah: TextView
    private lateinit var txtEspStatus: TextView
    private lateinit var floorMapContainer: FrameLayout

    private lateinit var btnLt1: TextView
    private lateinit var btnLt2: TextView
    private lateinit var btnLt3: TextView
    private lateinit var btnLt5: TextView

    private var currentFloor = 1

    private lateinit var wifiPositioningManager: WifiPositioningManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var gpsCallback: LocationCallback? = null

    private val positionListener = object : WifiPositioningManager.PositionListener {
        override fun onPositionUpdated(
            normalizedX: Float,
            normalizedY: Float,
            confidence: Float,
            espStatus: String
        ) {
            placeMarker(normalizedX, normalizedY)
            val bar = when {
                confidence >= 1.0f -> "████"
                confidence >= 0.75f -> "███░"
                confidence >= 0.5f -> "██░░"
                else -> "█░░░"
            }
            txtEspStatus.text = "[$bar] $espStatus"
        }

        override fun onEspNotFound() {
            txtEspStatus.text = "⚠ Tidak ada sinyal ESP terdeteksi"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denah_gku1)

        val floor = intent.getStringExtra("FLOOR")?.toIntOrNull() ?: 1

        imgFloorMap       = findViewById(R.id.imgFloorMap)
        imgUserMarker     = findViewById(R.id.imgUserMarker)
        txtTitleDenah     = findViewById(R.id.txtTitleDenah)
        txtEspStatus      = findViewById(R.id.txtEspStatus)
        floorMapContainer = findViewById(R.id.floorMapContainer)

        btnLt1 = findViewById(R.id.btnLt1)
        btnLt2 = findViewById(R.id.btnLt2)
        btnLt3 = findViewById(R.id.btnLt3)
        btnLt5 = findViewById(R.id.btnLt5)

        wifiPositioningManager = WifiPositioningManager(this)
        fusedLocationClient    = LocationServices.getFusedLocationProviderClient(this)

        // Marker tersembunyi sampai GPS membuktikan user dekat GKU 1
        imgUserMarker.visibility = View.GONE

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        btnLt1.setOnClickListener { showFloor(1) }
        btnLt2.setOnClickListener { showFloor(2) }
        btnLt3.setOnClickListener { showFloor(3) }
        btnLt5.setOnClickListener { showFloor(5) }

        showFloor(floor)
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            wifiPositioningManager.startScanning(positionListener)
            startGpsProximityCheck()

            // Jika ESP sudah punya posisi valid sebelumnya, langsung tampilkan
            if (wifiPositioningManager.hasValidPosition) {
                placeMarker(
                    wifiPositioningManager.lastNormalizedX,
                    wifiPositioningManager.lastNormalizedY
                )
            }
        } else {
            requestLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        wifiPositioningManager.stopScanning()
        stopGpsProximityCheck()
    }

    @SuppressLint("MissingPermission")
    private fun startGpsProximityCheck() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 500L
        ).build()

        gpsCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return

                val results = FloatArray(1)
                Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    GKU1_LAT, GKU1_LON,
                    results
                )
                val isNear = results[0] <= NEAR_THRESHOLD_METERS

                if (!isNear) {
                    // User jauh dari GKU 1 → sembunyikan marker
                    imgUserMarker.visibility = View.GONE
                    txtEspStatus.text = "Kamu belum berada di area GKU 1"
                } else if (!wifiPositioningManager.hasValidPosition) {
                    // User dekat tapi ESP belum terdeteksi → tampilkan di tengah sementara
                    imgUserMarker.visibility = View.VISIBLE
                    placeMarker(0.5f, 0.5f)
                    txtEspStatus.text = "Mencari sinyal ESP..."
                }
                // Jika ESP sudah punya posisi valid, positionListener yang mengatur marker
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request, gpsCallback!!, Looper.getMainLooper()
        )
    }

    private fun stopGpsProximityCheck() {
        gpsCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            gpsCallback = null
        }
    }

    private fun placeMarker(normX: Float, normY: Float) {
        floorMapContainer.post {
            val containerW = floorMapContainer.width.toFloat()
            val containerH = floorMapContainer.height.toFloat()
            val markerW    = imgUserMarker.width.toFloat()
            val markerH    = imgUserMarker.height.toFloat()

            if (containerW == 0f || containerH == 0f) return@post

            imgUserMarker.x = normX * containerW - markerW / 2f
            imgUserMarker.y = normY * containerH - markerH / 2f
            imgUserMarker.visibility = View.VISIBLE
        }
    }

    private fun showFloor(floor: Int) {
        currentFloor = floor
        txtTitleDenah.text = "GKU 1 - Lantai $floor"

        val imageRes = when (floor) {
            1 -> R.drawable.gku1_lt1
            2 -> R.drawable.gku1_lt2
            3 -> R.drawable.gku1_lt3
            5 -> R.drawable.gku1_lt5
            else -> R.drawable.gku1_lt1
        }
        imgFloorMap.setImageResource(imageRes)
        updateFloorButtons()

        // Tampilkan posisi terakhir yang valid saat berpindah lantai
        if (wifiPositioningManager.hasValidPosition) {
            placeMarker(
                wifiPositioningManager.lastNormalizedX,
                wifiPositioningManager.lastNormalizedY
            )
        }
    }

    private fun updateFloorButtons() {
        val buttons = listOf(1 to btnLt1, 2 to btnLt2, 3 to btnLt3, 5 to btnLt5)
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
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQ_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            wifiPositioningManager.startScanning(positionListener)
            startGpsProximityCheck()
        }
    }

    companion object {
        private const val REQ_LOCATION          = 201
        private const val GKU1_LAT              = -6.928879
        private const val GKU1_LON              = 107.769631
        private const val NEAR_THRESHOLD_METERS = 150f
    }
}
