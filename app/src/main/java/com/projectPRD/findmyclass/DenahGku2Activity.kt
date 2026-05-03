package com.projectPRD.findmyclass

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DenahGku2Activity : AppCompatActivity() {

    private lateinit var imgFloorMap: ImageView
    private lateinit var imgUserMarker: ImageView
    private lateinit var txtTitleDenah: TextView

    private lateinit var btnLt1: TextView
    private lateinit var btnLt2: TextView
    private lateinit var btnLt3: TextView
    private lateinit var btnLt5: TextView

    private var currentFloor = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denah_gku2)

        imgFloorMap = findViewById(R.id.imgFloorMap)
        imgUserMarker = findViewById(R.id.imgUserMarker)
        txtTitleDenah = findViewById(R.id.txtTitleDenah)

        btnLt1 = findViewById(R.id.btnLt1)
        btnLt2 = findViewById(R.id.btnLt2)
        btnLt3 = findViewById(R.id.btnLt3)
        btnLt5 = findViewById(R.id.btnLt5)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnLt1.setOnClickListener { showFloor(1) }
        btnLt2.setOnClickListener { showFloor(2) }
        btnLt3.setOnClickListener { showFloor(3) }
        btnLt5.setOnClickListener { showFloor(5) }

        showFloor(1)
    }

    private fun showFloor(floor: Int) {
        currentFloor = floor

        txtTitleDenah.text = "GKU 2 - Lantai $floor"

        val imageRes = when (floor) {
            1 -> R.drawable.gku2_ltd
            2 -> R.drawable.gku2_lt1
            3 -> R.drawable.gku2_lt2
            5 -> R.drawable.gku2_lt3
            else -> R.drawable.gku2_ltd
        }

        imgFloorMap.setImageResource(imageRes)

        updateFloorButtons()
        updateUserPositionDummy(floor)
    }

    private fun updateFloorButtons() {
        val buttons = listOf(
            1 to btnLt1,
            2 to btnLt2,
            3 to btnLt3,
            5 to btnLt5
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

    private fun updateUserPositionDummy(floor: Int) {
        imgUserMarker.post {
            when (floor) {
                1 -> {
                    imgUserMarker.x = 220f
                    imgUserMarker.y = 220f
                }
                2 -> {
                    imgUserMarker.x = 170f
                    imgUserMarker.y = 190f
                }
                3 -> {
                    imgUserMarker.x = 245f
                    imgUserMarker.y = 230f
                }
                5 -> {
                    imgUserMarker.x = 200f
                    imgUserMarker.y = 170f
                }
            }
        }
    }
}