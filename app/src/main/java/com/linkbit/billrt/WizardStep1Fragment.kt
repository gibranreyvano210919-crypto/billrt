package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.linkbit.billrt.databinding.FragmentWizardStep1InfoDasarBinding

class WizardStep1Fragment : Fragment() {

    private var _binding: FragmentWizardStep1InfoDasarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TambahPelangganViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWizardStep1InfoDasarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Restore state if needed
        binding.etNamaPelanggan.setText(viewModel.namaPelanggan)
        binding.etAlamatPelanggan.setText(viewModel.alamatPelanggan)
        binding.etTeleponPelanggan.setText(viewModel.teleponPelanggan)
    }

    fun saveData(): Boolean {
        // Simple validation
        if (binding.etNamaPelanggan.text.isNullOrBlank()) {
            binding.etNamaPelanggan.error = "Nama tidak boleh kosong"
            return false
        }
        viewModel.namaPelanggan = binding.etNamaPelanggan.text.toString()
        viewModel.alamatPelanggan = binding.etAlamatPelanggan.text.toString()
        viewModel.teleponPelanggan = binding.etTeleponPelanggan.text.toString()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
