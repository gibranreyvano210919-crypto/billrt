package com.linkbit.billrt

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.linkbit.billrt.databinding.FragmentWizardStep1InfoDasarBinding

class WizardStep1InfoDasarFragment : Fragment(), WizardValidator {

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

        // Restore data from ViewModel
        binding.etNamaPelanggan.setText(viewModel.namaPelanggan.value)
        binding.etAlamat.setText(viewModel.alamat.value)
        binding.etTelepon.setText(viewModel.telepon.value)

        // Set up TextWatchers to update ViewModel in real-time
        binding.etNamaPelanggan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.namaPelanggan.value = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etAlamat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.alamat.value = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etTelepon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.telepon.value = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun validate(): String? {
        if (binding.etNamaPelanggan.text.isNullOrBlank()) {
            val errorMessage = "Nama pelanggan tidak boleh kosong"
            binding.etNamaPelanggan.error = errorMessage
            return errorMessage // Kembalikan pesan error
        }
        binding.etNamaPelanggan.error = null
        return null // Kembalikan null jika valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
