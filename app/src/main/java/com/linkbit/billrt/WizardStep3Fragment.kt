package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.linkbit.billrt.databinding.FragmentWizardStep3InfoTeknisBinding

class WizardStep3Fragment : Fragment() {

    private var _binding: FragmentWizardStep3InfoTeknisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TambahPelangganViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWizardStep3InfoTeknisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Restore state if needed
        binding.etLatitude.setText(viewModel.latitude?.toString())
        binding.etLongitude.setText(viewModel.longitude?.toString())
        binding.etMacAddress.setText(viewModel.macAddress)
    }

    fun saveData(): Boolean {
        viewModel.latitude = binding.etLatitude.text.toString().toDoubleOrNull()
        viewModel.longitude = binding.etLongitude.text.toString().toDoubleOrNull()
        viewModel.macAddress = binding.etMacAddress.text.toString()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
