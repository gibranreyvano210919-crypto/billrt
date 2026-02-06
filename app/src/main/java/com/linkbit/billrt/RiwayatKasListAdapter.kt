package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemRiwayatKasListBinding

class RiwayatKasListAdapter(
    private var items: List<RiwayatKasItem>
) : RecyclerView.Adapter<RiwayatKasListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatKasListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<RiwayatKasItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRiwayatKasListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatKasItem) {
            binding.tvNamaPelangganRiwayat.text = item.namaPelanggan
            binding.tvTanggalCatatRiwayat.text = item.tanggalCatat
            binding.tvNamaTeknisiRiwayat.text = "Teknisi: ${item.namaTeknisi ?: "N/A"}"
        }
    }
}