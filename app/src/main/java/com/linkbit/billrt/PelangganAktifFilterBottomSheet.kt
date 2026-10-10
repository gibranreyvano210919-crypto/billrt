package com.linkbit.billrt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linkbit.billrt.databinding.BottomSheetFilterPelangganAktifBinding

class PelangganAktifFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterPelangganAktifBinding? = null
    private val binding get() = _binding!!

    private var onWilayahSelected: ((Int?, String?) -> Unit)? = null
    private var summaryWilayahList: List<SummaryWilayahItem> = emptyList()
    private var selectedWilayahId: Int? = null

    companion object {
        fun newInstance(
            summary: List<SummaryWilayahItem>,
            selectedId: Int?
        ): PelangganAktifFilterBottomSheet {
            val fragment = PelangganAktifFilterBottomSheet()
            fragment.summaryWilayahList = summary
            fragment.selectedWilayahId = selectedId
            return fragment
        }
    }

    fun setOnWilayahSelectedListener(listener: (Int?, String?) -> Unit) {
        onWilayahSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterPelangganAktifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val options = mutableListOf<Pair<Int?, String>>()
        options.add(null to "Semua Wilayah")

        summaryWilayahList.forEach { summary ->
            options.add(summary.idWilayah.toIntOrNull() to "${summary.namaWilayah} (${summary.total})")
        }

        binding.rvWilayahFilter.layoutManager = LinearLayoutManager(context)
        binding.rvWilayahFilter.adapter = WilayahFilterAdapter(options, selectedWilayahId) { id, name ->
            onWilayahSelected?.invoke(id, name)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class WilayahFilterAdapter(
        private val items: List<Pair<Int?, String>>,
        private val selectedId: Int?,
        private val onClick: (Int?, String) -> Unit
    ) : RecyclerView.Adapter<WilayahFilterAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.tv_wilayah_name)
            val lineSeparator: View = view.findViewById(R.id.line_separator) ?: view // Jika ada id khusus untuk garis
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_wilayah_filter, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textView.text = item.second
            
            // Highlight selected item
            if (item.first == selectedId) {
                holder.textView.setTypeface(null, android.graphics.Typeface.BOLD)
                holder.textView.setTextColor(holder.itemView.context.getColor(R.color.purple_500))
            } else {
                holder.textView.setTypeface(null, android.graphics.Typeface.NORMAL)
                holder.textView.setTextColor(holder.itemView.context.getColor(android.R.color.black))
            }

            // Set click listener pada TextView agar ripple background berjalan
            holder.textView.setOnClickListener {
                onClick(item.first, item.second)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
