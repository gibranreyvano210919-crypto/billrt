package com.linkbit.billrt

import android.app.Application
import android.widget.Toast
import com.cloudinary.android.MediaManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCloudinary()
    }

    private fun setupCloudinary() {
        val config = mapOf(
            "cloud_name" to "dbqwn9fcr",
            "api_key" to "659683344485174",
            "api_secret" to "97OJC46y3FKwrrH3zE1INElGJ4Q"
        )
        // Pengecekan sederhana untuk memastikan konfigurasi tidak placeholder
        if (config["cloud_name"] == "NAMA_CLOUD_ANDA"){
            Toast.makeText(this, "Konfigurasi Cloudinary belum diatur!", Toast.LENGTH_LONG).show()
        } else {
            MediaManager.init(this, config)
        }
    }
}
