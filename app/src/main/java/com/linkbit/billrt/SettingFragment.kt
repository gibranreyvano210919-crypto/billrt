package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.linkbit.billrt.databinding.FragmentSettingBinding

class SettingFragment : BaseFragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        applyWindowInsets(binding.toolbar)
        setupUserData()

        val settingItems = listOf(
            SettingItem("pengaturan_perusahaan", "Pengaturan Perusahaan", R.drawable.ic_business),
            SettingItem("master_wilayah", "Master Wilayah", R.drawable.ic_map),
            SettingItem("master_paket", "Master Paket Internet", R.drawable.ic_router),
            SettingItem("generate_tagihan", "Generate Tagihan", R.drawable.ic_receipt),
            SettingItem("setting_lokasi", "Pengaturan Lokasi", R.drawable.ic_location),
            SettingItem("pilih_printer", "Pilih Printer Bluetooth", R.drawable.ic_print),
            SettingItem("info_akun", "Informasi Akun", R.drawable.ic_person),
            SettingItem("cek_update", "Cek Update", R.drawable.ic_update),
            SettingItem("logout", "Logout", R.drawable.ic_logout)
        )

        val settingAdapter = SettingAdapter(settingItems) { selectedItem ->
            when (selectedItem.id) {
                "pengaturan_perusahaan" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_pengaturanFragment)
                }
                "master_wilayah" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_masterWilayahFragment)
                }
                "master_paket" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_masterPaketFragment)
                }
                "generate_tagihan" -> {
                    findNavController().navigate(R.id.action_settingFragment_to_periodeTagihanFragment)
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
                    sessionManager.logoutUser()
                    findNavController().navigate(R.id.loginFragment)
                    Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingAdapter
            isNestedScrollingEnabled = false
        }

        binding.tvAppVersion.text = "Versi ${BuildConfig.VERSION_NAME}"
    }

    private fun setupUserData() {
        binding.tvUserName.text = sessionManager.getUserName() ?: "Administrator"
        // Anda bisa menambahkan role jika tersedia di sessionManager
        binding.tvUserRole.text = "Petugas Lapangan"
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
