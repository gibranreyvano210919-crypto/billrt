package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.FragmentPelangganSearchBinding

class PelangganSearchFragment(
    private val pelangganList: List<PelangganMapData>,
    private val onPelangganSelected: (PelangganMapData) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPelangganSearchBinding
    private lateinit var adapter: PelangganSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPelangganSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PelangganSearchAdapter(pelangganList) { pelanggan ->
            onPelangganSelected(pelanggan)
            dismiss()
        }
        binding.recyclerViewPelanggan.adapter = adapter
        binding.recyclerViewPelanggan.layoutManager = LinearLayoutManager(context)

        binding.searchViewPelanggan.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrBlank()) {
                    pelangganList
                } else {
                    pelangganList.filter { it.nama.contains(newText, ignoreCase = true) }
                }
                adapter.updateData(filteredList)
                return true
            }
        })
    }

    companion object {
        fun newInstance(pelangganList: List<PelangganMapData>, onPelangganSelected: (PelangganMapData) -> Unit): PelangganSearchFragment {
            return PelangganSearchFragment(pelangganList, onPelangganSelected)
        }
    }
}
