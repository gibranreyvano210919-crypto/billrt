package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.PelangganListItem
import com.linkbit.billrt.databinding.ItemPelangganBinding

class PelangganListAdapter(
    private var pelangganList: List<PelangganListItem>,
    private val onItemClick: (PelangganListItem) -> Unit
) : RecyclerView.Adapter<PelangganListAdapter.PelangganViewHolder>() {

    private var filteredPelangganList: List<PelangganListItem> = pelangganList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val binding = ItemPelangganBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PelangganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        holder.bind(filteredPelangganList[position])
    }

    override fun getItemCount(): Int = filteredPelangganList.size

    inner class PelangganViewHolder(private val binding: ItemPelangganBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganListItem) {
            binding.nama.text = pelanggan.namaPelanggan
            binding.alamat.text = pelanggan.mikrotikUsername
            itemView.setOnClickListener { onItemClick(pelanggan) }
        }
    }

    fun updateList(newList: List<PelangganListItem>) {
        pelangganList = newList
        filter("") // Apply empty filter to show all initial data
    }

    fun filter(query: String) {
        filteredPelangganList = if (query.isEmpty()) {
            pelangganList
        } else {
            val lowerCaseQuery = query.lowercase()
            pelangganList.filter {
                it.namaPelanggan.lowercase().contains(lowerCaseQuery) ||
                it.mikrotikUsername?.lowercase()?.contains(lowerCaseQuery) == true
            }
        }
        notifyDataSetChanged()
    }
}
