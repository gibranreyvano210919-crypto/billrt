package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemRekapTunggakanLanjutanBinding
import com.linkbit.billrt.model.RekapTunggakanLanjutan

class RekapTunggakanLanjutanAdapter(
    private val items: List<RekapTunggakanLanjutan>
) : RecyclerView.Adapter<RekapTunggakanLanjutanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRekapTunggakanLanjutanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemRekapTunggakanLanjutanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RekapTunggakanLanjutan) {
            binding.tvNamaPelanggan.text = item.nama
            binding.tvUsernameMikrotik.text = item.username
            binding.tvWilayah.text = item.wilayah
            binding.tvTanggalTerakhir.text = "Tanggal: ${item.tgl_terakhir ?: "-"}"
            binding.tvTeknisiTerakhir.text = "Teknisi: ${item.teknisi_prev ?: "-"}"
            binding.tvPeriodeTerakhir.text = "Periode: ${item.periode_lalu ?: "Baru"}"
        }
    }
}