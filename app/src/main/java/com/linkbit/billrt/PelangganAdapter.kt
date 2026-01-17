package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganBinding

class PelangganAdapter(
    private val pelangganList: List<PelangganData>,
    private val onItemClick: (PelangganData) -> Unit
) : RecyclerView.Adapter<PelangganAdapter.PelangganViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val binding = ItemPelangganBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PelangganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.bind(pelanggan)
        holder.itemView.setOnClickListener { onItemClick(pelanggan) }
    }

    override fun getItemCount() = pelangganList.size

    class PelangganViewHolder(private val binding: ItemPelangganBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganData) {
            binding.tvNamaPelanggan.text = pelanggan.namaPelanggan
            binding.tvIdPelanggan.text = "ID: ${pelanggan.idPelanggan}"
            binding.tvMikrotikUsername.text = pelanggan.mikrotikUsername
            binding.tvWilayah.text = pelanggan.namaWilayah
            binding.tvInstallationDate.text = pelanggan.installationDate
            binding.tvTelepon.text = pelanggan.telepon

            // Menampilkan ikon lokasi hanya jika koordinat valid
            val hasValidCoordinates = pelanggan.latitude != null && pelanggan.latitude != "0.00000000" &&
                                    pelanggan.longitude != null && pelanggan.longitude != "0.00000000"
            
            binding.ivLocationPin.visibility = if (hasValidCoordinates) View.VISIBLE else View.GONE
        }
    }
}
