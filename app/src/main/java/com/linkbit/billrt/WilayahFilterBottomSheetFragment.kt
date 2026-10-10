package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.adapter.WilayahFilterAdapter
import com.linkbit.billrt.databinding.BottomSheetFilterWilayahBinding
import com.linkbit.billrt.viewmodel.PelangganBelumBayarViewModel

class WilayahFilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterWilayahBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PelangganBelumBayarViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterWilayahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Mengambil ViewModel dari Activity agar datanya sinkron dengan BelumBayarFragment
        viewModel = ViewModelProvider(requireActivity()).get(PelangganBelumBayarViewModel::class.java)

        setupRecyclerView()
        
        binding.buttonShowAll.setOnClickListener {
            setFragmentResult("filter_wilayah", bundleOf(
                "id_wilayah" to null, 
                "nama_wilayah" to "Semua Wilayah"
            ))
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        viewModel.summaryWilayah.observe(viewLifecycleOwner) { summaryList ->
            if (summaryList.isNullOrEmpty()) return@observe

            binding.rvWilayahFilter.layoutManager = LinearLayoutManager(context)
            
            // Format data summary ke format yang dibutuhkan adapter (Pair<String, Int>)
            val displayList = summaryList.map { it.namaWilayah to it.total }
            
            val filterAdapter = WilayahFilterAdapter(displayList) { selectedWilayahName ->
                val selectedItem = summaryList.find { it.namaWilayah == selectedWilayahName }
                val idWilayah = selectedItem?.idWilayah?.toIntOrNull()
                
                setFragmentResult("filter_wilayah", bundleOf(
                    "id_wilayah" to idWilayah,
                    "nama_wilayah" to selectedWilayahName
                ))
                dismiss()
            }
            binding.rvWilayahFilter.adapter = filterAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WilayahFilterBottomSheet"
        fun newInstance(): WilayahFilterBottomSheetFragment = WilayahFilterBottomSheetFragment()
    }
}
