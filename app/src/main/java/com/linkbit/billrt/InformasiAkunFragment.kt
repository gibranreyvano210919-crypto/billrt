package com.linkbit.billrt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentInformasiAkunBinding

class InformasiAkunFragment : BaseFragment() {

    private var _binding: FragmentInformasiAkunBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformasiAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)
        setupToolbar()

        // Get user info from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val namaUser = sharedPreferences.getString("USER_NAME", "N/A")
        val username = sharedPreferences.getString("USER_USERNAME", "N/A")

        // Get app version from BuildConfig
        val appVersion = BuildConfig.VERSION_NAME

        // Set the user and version text
        binding.tvNamaUser.text = namaUser
        binding.tvUsername.text = username
        binding.tvAppVersion.text = "Versi Aplikasi: $appVersion"

        // --- Build Configuration Info String ---
        val configInfo = buildString {
            append("Base URL: ${ApiConfig.BASE_URL}\n\n")
            append("Firebase DB: https://mikrotik-alert-default-rtdb.asia-southeast1.firebasedatabase.app/\n\n")
            append("Firebase API Key: AIzaSyCxjftKa70YPfRXbYwC73-O0riEF0QiDeg\n\n")
            append("Engine Peta: OpenStreetMap (osmdroid)\n\n")
            val cloudinaryUrl = "cloudinary://659683344485174:97OJC46y3FKwrrH3zE1INElGJ4Q@dbqwn9fcr"
            append("Cloudinary: $cloudinaryUrl")
        }

        binding.tvConfigInfo.text = configInfo
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Informasi Akun"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
