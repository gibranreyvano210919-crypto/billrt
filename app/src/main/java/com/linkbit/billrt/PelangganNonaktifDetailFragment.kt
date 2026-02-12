package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.linkbit.billrt.databinding.FragmentPelangganNonaktifDetailBinding

class PelangganNonaktifDetailFragment : BaseFragment() {

    private var _binding: FragmentPelangganNonaktifDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PelangganNonaktifDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPelangganNonaktifDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pelangganId = args.pelangganId
        // TODO: Fetch and display a real customer's data using the pelangganId
        binding.tvNamaPelanggan.text = "Nama Pelanggan Nonaktif"
        binding.tvIdPelanggan.text = "ID: $pelangganId"
        binding.tvAlamat.text = "Alamat: Alamat Pelanggan Nonaktif"
        binding.tvTelepon.text = "Telepon: 081234567890"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}