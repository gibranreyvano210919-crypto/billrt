package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganMapsBinding
import com.linkbit.billrt.model.PelangganData

class PelangganMapsAdapter(private var pelangganList: List<PelangganData>) : RecyclerView.Adapter<PelangganMapsAdapter.PelangganViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val binding = ItemPelangganMapsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PelangganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount(): Int = pelangganList.size

    fun updateData(newPelangganList: List<PelangganData>) {
        pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    inner class PelangganViewHolder(private val binding: ItemPelangganMapsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganData) {
            binding.namaTextView.text = pelanggan.nama ?: "N/A"
            binding.userTextView.text = "User: ${pelanggan.idPelanggan ?: "N/A"}"
            binding.wilayahTextView.text = "Wilayah: N/A"
            binding.paketTextView.text = "Paket: N/A"
            binding.koordinatTextView.text = "Koordinat tidak tersedia"
        }
    }
}
