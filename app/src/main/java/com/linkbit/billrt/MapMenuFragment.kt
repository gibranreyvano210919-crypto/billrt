package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentMapMenuBinding

class MapMenuFragment : BaseFragment() {

    private var _binding: FragmentMapMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Perbaikan posisi toolbar: terapkan insets hanya pada AppBarLayout
        applyWindowInsets(binding.appBarLayout)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.cardPetaPelanggan.setOnClickListener {
            findNavController().navigate(R.id.action_mapMenuFragment_to_mapPelangganFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
