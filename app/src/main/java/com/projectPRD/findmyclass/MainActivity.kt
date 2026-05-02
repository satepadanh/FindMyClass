package com.projectPRD.findmyclass

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)
        val markerGku1 = findViewById<FrameLayout>(R.id.markerGku1)
        val markerGku2 = findViewById<FrameLayout>(R.id.markerGku2)

        val infoCard = findViewById<LinearLayout>(R.id.infoCard)
        val txtGedung = findViewById<TextView>(R.id.txtGedung)

        mapContainer.post {
            val width = mapContainer.width
            val height = mapContainer.height

            markerGku1.x = width * 0.70f - markerGku1.width / 2
            markerGku1.y = height * 0.53f - markerGku1.height

            markerGku2.x = width * 0.52f - markerGku2.width / 2
            markerGku2.y = height * 0.58f - markerGku2.height
        }

        markerGku1.setOnClickListener {
            txtGedung.text = "GKU 1"
            infoCard.visibility = View.VISIBLE
        }

        markerGku2.setOnClickListener {
            txtGedung.text = "GKU 2"
            infoCard.visibility = View.VISIBLE
        }
    }
}