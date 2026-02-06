package com.linkbit.billrt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.linkbit.billrt.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Padding atas untuk status bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)

            // Padding bawah untuk Bottom Navigation agar ikon tidak tertutup garis gestur
            binding.bottomNavView.setPadding(binding.bottomNavView.paddingLeft, binding.bottomNavView.paddingTop, binding.bottomNavView.paddingRight, systemBars.bottom)

            insets
        }

        // Find the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup BottomNavigationView with NavController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)

        // Add a destination change listener to show/hide the bottom nav
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // List of top-level destinations where the bottom nav should be visible
                R.id.berandaFragment,
                R.id.smartOltMonitorFragment,
                R.id.pencarianCepatFragment,
                R.id.nav_maps,
                R.id.settingFragment -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                }
                // Hide on all other destinations (like Login, Details, Wizards)
                else -> {
                    binding.bottomNavView.visibility = View.GONE
                }
            }
        }

        createNotificationChannel()

        // Handle notification intent
        val logId = intent.getStringExtra("log_id")
        if (!logId.isNullOrEmpty()) {
            fetchLogDetail(logId)
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        val logId = intent?.getStringExtra("log_id")
        if (!logId.isNullOrEmpty()) {
            fetchLogDetail(logId)
        }
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
                    .setMessage(
                        "Router: $routerName\n" +
                        "Waktu: $time\n\n" +
                        "Pesan Error:\n$errorMsg"
                    )
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
                "default_channel_id",
                "Notifikasi Monitoring",
                NotificationManager.IMPORTANCE_HIGH
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
        } catch (e: Exception) {
            dateString // Return original string if parsing fails
        }
    }

    // This allows fragments to navigate up when the system back button is pressed
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
