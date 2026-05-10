package com.projectPRD.findmyclass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import kotlin.math.pow
import android.annotation.SuppressLint

/**
 * Mendeteksi posisi user di dalam gedung GKU 1 Lantai 3
 * menggunakan kekuatan sinyal (RSSI) dari 4 ESP8266 di tiap sudut.
 *
 * Layout fisik ESP di lantai:
 *   [esp-k1-9675] ----(atas)---- [esp-k1-9678]
 *        |                             |
 *   [esp-k1-9676] ----(bawah)--- [esp-k1-9677]
 *
 * Algoritma: Weighted Centroid
 *   - RSSI → jarak via path loss formula
 *   - Bobot = 1 / jarak²
 *   - Posisi = rata-rata berbobot dari koordinat sudut
 */
class WifiPositioningManager(private val context: Context) {

    interface PositionListener {
        /**
         * Dipanggil saat posisi berhasil dihitung.
         * @param normalizedX posisi horizontal 0.0 (kiri) – 1.0 (kanan)
         * @param normalizedY posisi vertikal 0.0 (atas) – 1.0 (bawah)
         * @param confidence  0.0–1.0, seberapa banyak ESP terdeteksi (1.0 = semua 4 ESP)
         */
        fun onPositionUpdated(normalizedX: Float, normalizedY: Float, confidence: Float, espStatus: String = "")

        /** Dipanggil jika tidak ada satu pun ESP yang terdeteksi */
        fun onEspNotFound()
    }

    companion object {
        /**
         * Peta SSID → posisi ternormalisasi (x, y) dalam denah.
         * x: 0=kiri, 1=kanan | y: 0=atas, 1=bawah
         */
        private val ESP_POSITIONS = mapOf(
            "esp-k1-9675" to Pair(0f, 0f),  // sudut kiri-atas
            "esp-k1-9678" to Pair(1f, 0f),  // sudut kanan-atas
            "esp-k1-9676" to Pair(0f, 1f),  // sudut kiri-bawah
            "esp-k1-9677" to Pair(1f, 1f)   // sudut kanan-bawah
        )

        // RSSI yang diukur pada jarak 1 meter dari ESP8266 (estimasi standar)
        private const val TX_POWER = -65f

        // Path loss exponent untuk lingkungan indoor (2.0=ruang terbuka, 3.0=banyak tembok)
        private const val PATH_LOSS_N = 2.5f

        // Jeda antar scan (ms). Android 9+ membatasi ~4 scan/2menit;
        // 10 detik aman dan hemat baterai.
        private const val SCAN_INTERVAL_MS = 10_000L
    }

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val handler = Handler(Looper.getMainLooper())

    private var listener: PositionListener? = null
    private var isScanning = false

    // Posisi terakhir yang valid (dipakai saat berpindah lantai)
    var lastNormalizedX = 0.5f
        private set
    var lastNormalizedY = 0.5f
        private set
    var hasValidPosition = false
        private set

    // BroadcastReceiver menerima hasil scan WiFi dari OS
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                processScanResults()
                // Jadwalkan scan berikutnya setelah delay
                if (isScanning) {
                    handler.postDelayed(startScanRunnable, SCAN_INTERVAL_MS)
                }
            }
        }
    }

    // Runnable yang memulai scan WiFi
    private val startScanRunnable = Runnable {
        if (isScanning) {
            @Suppress("DEPRECATION")
            wifiManager.startScan()
        }
    }

    /**
     * Mulai scan WiFi. Aman dipanggil berulang kali (idempotent).
     * Harus dipanggil dari onResume() Activity.
     */
    fun startScanning(listener: PositionListener) {
        if (isScanning) {
            // Hanya perbarui listener jika sudah berjalan
            this.listener = listener
            return
        }
        this.listener = listener
        isScanning = true

        context.registerReceiver(
            scanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        // Mulai scan pertama segera
        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }

    /**
     * Hentikan scan. Harus dipanggil dari onPause() Activity.
     */
    fun stopScanning() {
        if (!isScanning) return
        isScanning = false
        listener = null
        handler.removeCallbacks(startScanRunnable)
        try {
            context.unregisterReceiver(scanReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver sudah di-unregister, abaikan
        }
    }

    @SuppressLint("MissingPermission")
    private fun processScanResults() {
        val allResults = wifiManager.scanResults

        val espResults = allResults
            .filter { ESP_POSITIONS.containsKey(it.SSID?.removeSurrounding("\"")?.trim()) }
            .sortedByDescending { it.level } // urutkan dari sinyal terkuat

        if (espResults.isEmpty()) {
            listener?.onEspNotFound()
            return
        }

        val strongest = espResults[0]
        val strongestSsid = strongest.SSID?.removeSurrounding("\"")?.trim() ?: return
        val strongestPos = ESP_POSITIONS[strongestSsid] ?: return

        // Cek apakah ada ESP kedua dengan kekuatan hampir sama (selisih <= 5 dBm)
        val second = espResults.getOrNull(1)
        val finalX: Float
        val finalY: Float

        if (second != null && (strongest.level - second.level) <= 5) {
            val secondSsid = second.SSID?.removeSurrounding("\"")?.trim() ?: ""
            val secondPos = ESP_POSITIONS[secondSsid]
            if (secondPos != null) {
                // Dua ESP hampir sama kuat → posisi di tengah keduanya
                finalX = (strongestPos.first + secondPos.first) / 2f
                finalY = (strongestPos.second + secondPos.second) / 2f
            } else {
                finalX = strongestPos.first
                finalY = strongestPos.second
            }
        } else {
            // Satu ESP paling kuat → posisi di sudut ESP tersebut
            // Sedikit offset ke dalam agar marker tidak keluar denah
            finalX = strongestPos.first.coerceIn(0.05f, 0.95f)
            finalY = strongestPos.second.coerceIn(0.05f, 0.95f)
        }

        lastNormalizedX = finalX
        lastNormalizedY = finalY
        hasValidPosition = true

        val confidence = espResults.size.toFloat() / ESP_POSITIONS.size.toFloat()

        // Kirim juga raw RSSI untuk status display
        val statusText = espResults.joinToString("  |  ") { scan ->
            val ssid = scan.SSID?.removeSurrounding("\"")?.trim() ?: ""
            val shortName = ssid.replace("esp-k1-", "ESP-")
            "$shortName: ${scan.level}dBm"
        }

        listener?.onPositionUpdated(finalX, finalY, confidence, statusText)
    }

    /**
     * Konversi RSSI (dBm) → estimasi jarak (meter)
     * Rumus: distance = 10 ^ ((txPower - rssi) / (10 * n))
     */
    private fun rssiToDistance(rssi: Int): Float {
        return 10f.pow((TX_POWER - rssi.toFloat()) / (10f * PATH_LOSS_N))
    }
}
