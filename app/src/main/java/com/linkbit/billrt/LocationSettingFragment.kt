package com.linkbit.billrt

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linkbit.billrt.databinding.FragmentLocationSettingBinding

class LocationSettingFragment : Fragment() {

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

        // Set a listener on the switch
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            // Open location settings regardless of whether the switch is turned on or off
            // This is the standard and safest behavior as apps cannot toggle GPS directly.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
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
