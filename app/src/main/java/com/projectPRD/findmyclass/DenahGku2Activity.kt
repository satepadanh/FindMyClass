package com.projectPRD.findmyclass

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DenahGku2Activity : AppCompatActivity() {

    private lateinit var imgFloorMap: ImageView
    private lateinit var imgUserMarker: ImageView
    private lateinit var txtTitleDenah: TextView

    private lateinit var btnLtd: TextView
    private lateinit var btnLt1: TextView
    private lateinit var btnLt2: TextView
    private lateinit var btnLt3: TextView

    private var currentFloor = "LTD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denah_gku2)

        val floor = intent.getStringExtra("FLOOR") ?: "LTD"

        imgFloorMap = findViewById(R.id.imgFloorMap)
        imgUserMarker = findViewById(R.id.imgUserMarker)
        txtTitleDenah = findViewById(R.id.txtTitleDenah)

        btnLtd = findViewById(R.id.btnLtd)
        btnLt1 = findViewById(R.id.btnLt1)
        btnLt2 = findViewById(R.id.btnLt2)
        btnLt3 = findViewById(R.id.btnLt3)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnLtd.setOnClickListener { showFloor("LTD") }
        btnLt1.setOnClickListener { showFloor("LT1") }
        btnLt2.setOnClickListener { showFloor("LT2") }
        btnLt3.setOnClickListener { showFloor("LT3") }

        showFloor(floor)
    }

    private fun showFloor(floor: String) {
        currentFloor = floor

        txtTitleDenah.text = when (floor) {
            "LTD" -> "GKU 2 - Lantai Dasar"
            "LT1" -> "GKU 2 - Lantai 1"
            "LT2" -> "GKU 2 - Lantai 2"
            "LT3" -> "GKU 2 - Lantai 3"
            else -> "GKU 2 - Lantai Dasar"
        }

        val imageRes = when (floor) {
            "LTD" -> R.drawable.gku2_ltd
            "LT1" -> R.drawable.gku2_lt1
            "LT2" -> R.drawable.gku2_lt2
            "LT3" -> R.drawable.gku2_lt3
            else -> R.drawable.gku2_ltd
        }

        imgFloorMap.setImageResource(imageRes)

        updateFloorButtons()
        updateUserPositionDummy(floor)
    }

    private fun updateFloorButtons() {
        val buttons = listOf(
            "LTD" to btnLtd,
            "LT1" to btnLt1,
            "LT2" to btnLt2,
            "LT3" to btnLt3
        )

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

    private fun updateUserPositionDummy(floor: String) {
        imgUserMarker.post {
            when (floor) {
                "LTD" -> {
                    imgUserMarker.x = 220f
                    imgUserMarker.y = 220f
                }
                "LT1" -> {
                    imgUserMarker.x = 170f
                    imgUserMarker.y = 190f
                }
                "LT2" -> {
                    imgUserMarker.x = 245f
                    imgUserMarker.y = 230f
                }
                "LT3" -> {
                    imgUserMarker.x = 200f
                    imgUserMarker.y = 170f
                }
            }
        }
    }
}