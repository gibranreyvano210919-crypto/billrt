package com.linkbit.billrt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.linkbit.billrt.databinding.ActivityMainBinding
import com.linkbit.billrt.network.ApiClient
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    val apiService: ApiService by lazy {
        ApiClient.instance
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Masuk ke mode Edge-to-Edge agar konten bisa masuk ke bawah Status Bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Buat Status Bar sistem transparan total
        window.statusBarColor = Color.TRANSPARENT
        
        // 3. Ikon Status Bar tetap putih (false) agar kontras dengan gradient ungu gelap
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = false

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)

        // 4. Kontrol visibilitas BottomNav hanya untuk Beranda, Transaksi, Pencarian, Maps, dan Setting
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.berandaFragment,
                R.id.transaksiFragment,
                R.id.pencarianCepatFragment,
                R.id.nav_maps,
                R.id.settingFragment -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavView.visibility = View.GONE
                }
            }
        }

        createNotificationChannel()
        val logId = intent.getStringExtra("log_id")
        if (!logId.isNullOrEmpty()) fetchLogDetail(logId)
    }

    private fun fetchLogDetail(id: String) {
        val dbUrl = "https://mikrotik-alert-default-rtdb.asia-southeast1.firebasedatabase.app/"
        val ref = FirebaseDatabase.getInstance(dbUrl).getReference("logs/errors").child(id)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val routerName = snapshot.child("router_name").value.toString()
                val errorMsg = snapshot.child("error").value.toString()
                val time = snapshot.child("time").value.toString()

                MaterialAlertDialogBuilder(this)
                    .setTitle("Detail Alert MikroTik")
                    .setMessage("Router: $routerName\nWaktu: $time\n\nPesan Error:\n$errorMsg")
                    .setPositiveButton("Tutup", null)
                    .show()
            }
        }.addOnFailureListener {
            Log.e("FCM_ERROR", "Gagal mengambil detail log: ${it.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel_id", "Notifikasi Monitoring", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) { dateString }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
