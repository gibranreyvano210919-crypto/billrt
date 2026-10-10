package com.linkbit.billrt

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentLocationSettingBinding

class LocationSettingFragment : BaseFragment() {

    private var _binding: FragmentLocationSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets(binding.appBarLayout)
        setupToolbar()

        // Set a listener on the switch
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            // Open location settings regardless of whether the switch is turned on or off
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Pengaturan Lokasi"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check the actual status when the user returns to the fragment
        checkLocationStatus()
    }

    private fun checkLocationStatus() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                                  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        // Update the switch state without triggering the listener
        binding.switchLocation.setOnCheckedChangeListener(null) // Temporarily disable listener
        binding.switchLocation.isChecked = isLocationEnabled
        binding.switchLocation.setOnCheckedChangeListener { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } // Re-enable listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
