package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganSearchBinding

class PelangganSearchAdapter(
    private var pelangganList: List<PelangganMapData>,
    private val onPelangganClicked: (PelangganMapData) -> Unit
) : RecyclerView.Adapter<PelangganSearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.binding.tvNamaPelanggan.text = pelanggan.nama
        holder.binding.tvAlamatPelanggan.text = pelanggan.alamat
        holder.binding.tvLat.text = "Lat: ${pelanggan.lat ?: "N/A"}"
        holder.binding.tvLng.text = "Lng: ${pelanggan.lng ?: "N/A"}"
        holder.itemView.setOnClickListener { onPelangganClicked(pelanggan) }
    }

    override fun getItemCount() = pelangganList.size

    fun updateData(newPelangganList: List<PelangganMapData>) {
        this.pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemPelangganSearchBinding) : RecyclerView.ViewHolder(binding.root)
}
