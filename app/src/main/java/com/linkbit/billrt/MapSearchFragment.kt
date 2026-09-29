package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.linkbit.billrt.databinding.FragmentMapSearchBinding
import com.mapbox.maps.Style

class MapSearchFragment : BaseFragment() {

    private var _binding: FragmentMapSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val appBarLayout = binding.toolbarMapSearch.parent as? View
        appBarLayout?.let { applyWindowInsets(it) }
        
        setupToolbar()
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarMapSearch)
        binding.toolbarMapSearch.setupWithNavController(findNavController())
        binding.toolbarMapSearch.title = "Cari Lokasi"
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }
}
