package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.linkbit.billrt.databinding.FragmentMapMenuBinding

class MapMenuFragment : Fragment() {

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

        binding.cardPetaPelanggan.setOnClickListener {
            findNavController().navigate(R.id.action_mapMenuFragment_to_mapPelangganFragment)
        }

        binding.cardGantiLokasi.setOnClickListener {
            findNavController().navigate(R.id.action_mapMenuFragment_to_gantiLokasiFragment)
        }

        binding.cardPetaOdp.setOnClickListener {
            findNavController().navigate(R.id.action_mapMenuFragment_to_odpMapFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
