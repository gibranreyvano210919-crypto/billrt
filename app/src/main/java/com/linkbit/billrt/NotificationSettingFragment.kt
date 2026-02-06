package com.linkbit.billrt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.linkbit.billrt.databinding.FragmentNotifikasiSettingBinding

class NotifikasiSettingFragment : Fragment() {

    private var _binding: FragmentNotifikasiSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationPrefRef: DatabaseReference
    private var userId: Int = -1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Setelah izin diberikan, coba aktifkan lagi di Firebase
            updateFirebaseNotification(true)
        } else {
            Toast.makeText(context, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotifikasiSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId == -1) {
            binding.tvStatus.text = "Sesi pengguna tidak valid."
            binding.switchNotification.isEnabled = false
            return
        }

        val dbUrl = "https://mikrotik-alert-default-rtdb.asia-southeast1.firebasedatabase.app/"
        notificationPrefRef = FirebaseDatabase.getInstance(dbUrl).getReference("users").child(userId.toString()).child("notification_enabled")

        setupSwitchListener()
    }

    override fun onResume() {
        super.onResume()
        syncSwitchFromServer()
    }

    private fun syncSwitchFromServer() {
        binding.tvStatus.text = "Menyinkronkan..."
        binding.switchNotification.isEnabled = false

        // 1. Periksa izin OS terlebih dahulu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                binding.switchNotification.isChecked = false
                binding.switchNotification.isEnabled = true
                binding.tvStatus.text = "Izin notifikasi sistem belum aktif."
                return
            }
        }

        // 2. Jika izin sudah ada, baca dari Firebase
        notificationPrefRef.get().addOnSuccessListener { snapshot ->
            val isEnabled = snapshot.getValue(Boolean::class.java) ?: false // Default ke false jika null
            binding.switchNotification.isChecked = isEnabled
            binding.switchNotification.isEnabled = true
            binding.tvStatus.text = if (isEnabled) "Notifikasi Aktif" else "Notifikasi Nonaktif"
        }.addOnFailureListener { error ->
            binding.switchNotification.isEnabled = false
            binding.tvStatus.text = "Gagal sinkronisasi."
            Log.e(TAG, "Gagal membaca dari Firebase", error) // Log error untuk diagnosis
            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSwitchListener() {
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            // Minta izin jika mencoba mengaktifkan tanpa izin (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isChecked) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    binding.switchNotification.isChecked = false // Kembalikan state switch, tunggu hasil dialog
                    return@setOnCheckedChangeListener
                }
            }
            // Jika izin sudah ada (atau di bawah Android 13), langsung update
            updateFirebaseNotification(isChecked)
        }
    }

    private fun updateFirebaseNotification(isEnabled: Boolean) {
        notificationPrefRef.setValue(isEnabled)
            .addOnSuccessListener {
                binding.tvStatus.text = if (isEnabled) "Notifikasi Aktif" else "Notifikasi Nonaktif"
                Toast.makeText(context, "Pengaturan notifikasi disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Gagal menyimpan pengaturan", Toast.LENGTH_SHORT).show()
                binding.switchNotification.isChecked = !isEnabled // Kembalikan switch ke state sebelumnya
                Log.e(TAG, "Gagal menulis ke Firebase", error)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "NotifSettingFragment"
    }
}
