package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.PelangganMapData
import com.linkbit.billrt.databinding.ItemPelangganSearchBinding

class PelangganSearchAdapter(
    private var pelangganList: List<PelangganMapData>,
    private val onClick: (PelangganMapData) -> Unit
) : RecyclerView.Adapter<PelangganSearchAdapter.ViewHolder>() {

    fun updateData(newPelangganList: List<PelangganMapData>) {
        this.pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount() = pelangganList.size

    inner class ViewHolder(private val binding: ItemPelangganSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganMapData) {
            binding.tvNamaPelanggan.text = pelanggan.nama
            binding.tvAlamatPelanggan.text = pelanggan.alamat ?: "Alamat tidak tersedia"
            binding.tvLat.text = "Lat: ${pelanggan.lat ?: "N/A"}"
            binding.tvLng.text = "Lng: ${pelanggan.lng ?: "N/A"}"
            itemView.setOnClickListener { onClick(pelanggan) }
        }
    }
}
