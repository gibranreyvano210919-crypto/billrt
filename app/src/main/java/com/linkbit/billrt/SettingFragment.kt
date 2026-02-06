package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingItems = listOf(
            SettingItem("master_wilayah", "Master Wilayah", android.R.drawable.ic_menu_mapmode),
            SettingItem("master_paket", "Master Paket Internet", android.R.drawable.ic_menu_sort_by_size),
            SettingItem("setting_lokasi", "Pengaturan Lokasi", android.R.drawable.ic_menu_edit),
            SettingItem("pilih_printer", "Pilih Printer Bluetooth", android.R.drawable.ic_menu_save),
            SettingItem("info_akun", "Informasi Akun", android.R.drawable.ic_menu_myplaces),
            SettingItem("cek_update", "Cek Update", android.R.drawable.ic_menu_rotate),
            SettingItem("logout", "Logout", android.R.drawable.ic_lock_power_off)
        )

        val settingAdapter = SettingAdapter(settingItems) { selectedItem ->
            when (selectedItem.id) {
                "master_wilayah" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_masterWilayahFragment)
                }
                "master_paket" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_masterPaketFragment)
                }
                "pilih_printer" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_printerSettingFragment)
                }
                "setting_lokasi" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_locationSettingFragment)
                }
                "info_akun" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_informasiAkunFragment)
                }
                "cek_update" -> {
                    checkForUpdates()
                }
                "logout" -> {
                    // TODO: Implement logout logic (clear session, navigate to login)
                    findNavController().navigate(R.id.loginFragment)
                    Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingAdapter
        }
    }

    private fun checkForUpdates() {
        val currentVersion = BuildConfig.VERSION_NAME
        val latestVersion = "1.1.0" // Hardcoded latest version

        if (currentVersion != latestVersion) {
            Toast.makeText(context, "Update tersedia: $latestVersion", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Aplikasi sudah versi terbaru", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
