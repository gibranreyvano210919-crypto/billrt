package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPencarianCepatBinding

// NOTE: This adapter now expects com.linkbit.billrt.PelangganData

class PencarianCepatAdapter(
    private var items: List<PelangganData>,
    private val onCopyMacClick: (String) -> Unit,
    private val onRiwayatClick: (String) -> Unit,
    private val onRiwayatKasClick: (String) -> Unit
) : RecyclerView.Adapter<PencarianCepatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPencarianCepatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<PelangganData>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPencarianCepatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PelangganData) {
            binding.tvNamaPelanggan.text = item.nama
            binding.tvAlamat.text = item.alamat
            binding.tvWilayah.text = "Wilayah: ${item.namaWilayah ?: "N/A"}"
            binding.tvMikrotikUsername.text = "Username: ${item.mikrotikUsername ?: "N/A"}"
            binding.tvStaticIp.text = "IP: ${item.staticIp ?: "N/A"}"
            // Make sure macAddress from the correct data class is used
            binding.tvMacAddress.text = item.macAddress ?: "-"

            // Use the correct macAddress property
            if (!item.macAddress.isNullOrEmpty()) {
                binding.btnCopyMac.setOnClickListener { 
                    onCopyMacClick(item.macAddress)
                }
                binding.btnRiwayat.setOnClickListener { 
                    onRiwayatClick(item.macAddress)
                }
            } else {
                // Disable buttons if there is no MAC address
                binding.btnCopyMac.setOnClickListener(null)
                binding.btnRiwayat.setOnClickListener(null)
            }

            binding.btnRiwayatKas.setOnClickListener {
                onRiwayatKasClick(item.idPelanggan)
            }
        }
    }
}
