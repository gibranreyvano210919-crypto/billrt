package com.linkbit.billrt.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.linkbit.billrt.InputKasPelanggan
import com.linkbit.billrt.databinding.ItemPelangganChecklistBinding

class PelangganChecklistAdapter(
    private var originalList: List<InputKasPelanggan>,
    private val onSelectionChange: (List<InputKasPelanggan>) -> Unit
) : RecyclerView.Adapter<PelangganChecklistAdapter.ViewHolder>() {

    private var filteredList = mutableListOf<InputKasPelanggan>()
    private val selectedItems = mutableSetOf<InputKasPelanggan>()

    init {
        filteredList.addAll(originalList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateData(newList: List<InputKasPelanggan>) {
        originalList = newList
        filter(null)
    }

    fun filter(query: String?) {
        val currentQuery = query?.trim()
        filteredList.clear()
        if (currentQuery.isNullOrEmpty()) {
            filteredList.addAll(originalList)
        } else {
            originalList.forEach { pelanggan ->
                if (pelanggan.nama.contains(currentQuery, ignoreCase = true) || 
                    pelanggan.idPelanggan.contains(currentQuery, ignoreCase = true)) {
                    filteredList.add(pelanggan)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        onSelectionChange(emptyList())
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganChecklistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: InputKasPelanggan) {
            binding.cbPelanggan.text = "${pelanggan.nama} (${pelanggan.idPelanggan})"
            binding.tvAlamat.text = pelanggan.wilayah
            binding.cbPelanggan.setOnCheckedChangeListener(null) 
            binding.cbPelanggan.isChecked = selectedItems.contains(pelanggan)

            val cardView = itemView as? MaterialCardView
            if (pelanggan.isTercatat) {
                cardView?.setCardBackgroundColor(Color.parseColor("#E0F2F1"))
                binding.cbPelanggan.isEnabled = false
            } else {
                cardView?.setCardBackgroundColor(Color.WHITE)
                binding.cbPelanggan.isEnabled = true
            }

            binding.cbPelanggan.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(pelanggan)
                } else {
                    selectedItems.remove(pelanggan)
                }
                onSelectionChange(selectedItems.toList())
            }
        }
    }
}
