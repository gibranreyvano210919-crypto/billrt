package com.linkbit.billrt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetFilterBelumBayarBinding
import com.linkbit.billrt.viewmodel.PelangganBelumBayarViewModel

class BelumBayarFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBelumBayarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PelangganBelumBayarViewModel
    
    // Mapping Label Dropdown ke data ID dan Nama Wilayah asli
    private val labelToDataMap = mutableMapOf<String, Pair<Int?, String>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBelumBayarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Menggunakan ViewModel dari Activity agar sinkron dengan fragment utama
        viewModel = ViewModelProvider(requireActivity()).get(PelangganBelumBayarViewModel::class.java)

        setupDropdown()
        setupListeners()
    }

    private fun setupDropdown() {
        viewModel.summaryWilayah.observe(viewLifecycleOwner) { summaryList ->
            val labels = mutableListOf<String>()
            labelToDataMap.clear()

            // Opsi default
            val defaultLabel = "Semua Wilayah"
            labels.add(defaultLabel)
            labelToDataMap[defaultLabel] = Pair(null, "Semua Wilayah")

            summaryList.forEach { item ->
                // Buat label persis seperti yang akan dipilih user
                val label = "${item.namaWilayah} (${item.total})"
                labels.add(label)
                
                // Pastikan ID dikonversi ke Int sebelum disimpan
                val idInt = item.idWilayah.toIntOrNull()
                labelToDataMap[label] = Pair(idInt, item.namaWilayah)
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels)
            binding.acWilayah.setAdapter(adapter)
            
            // Set teks default jika belum ada pilihan agar tidak null saat klik Terapkan
            if (binding.acWilayah.text.isNullOrEmpty()) {
                binding.acWilayah.setText(defaultLabel, false)
            }
        }
    }

    private fun setupListeners() {
        binding.btnTerapkan.setOnClickListener {
            val selectedText = binding.acWilayah.text.toString()
            val selectedData = labelToDataMap[selectedText] ?: Pair(null, "Semua Wilayah")
            
            val idWilayah = selectedData.first
            val namaWilayah = selectedData.second

            Log.d("FilterDebug", "Terapkan -> ID: $idWilayah, Nama: $namaWilayah")

            // Kirim result ke Fragment utama. 
            // Fragment utama yang akan memicu fetch API agar parameter search & id_wilayah sinkron.
            setFragmentResult("filter_wilayah", bundleOf(
                "id_wilayah" to idWilayah,
                "nama_wilayah" to namaWilayah
            ))
            
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            Log.d("FilterDebug", "Reset Filter")
            setFragmentResult("filter_wilayah", bundleOf(
                "id_wilayah" to null,
                "nama_wilayah" to "Semua Wilayah"
            ))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BelumBayarFilterBottomSheet"
        fun newInstance() = BelumBayarFilterBottomSheet()
    }
}
