package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemNunggakTahunanBinding
import com.linkbit.billrt.model.PelangganTelatTahunanItem

class PelangganTelatTahunanAdapter(
    private val onItemClick: (PelangganTelatTahunanItem) -> Unit
) : ListAdapter<PelangganTelatTahunanItem, PelangganTelatTahunanAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNunggakTahunanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemNunggakTahunanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PelangganTelatTahunanItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvJumlahBulan.text = "${item.jumlahBulanTelat} Bulan"
            binding.tvWilayah.text = "Wilayah: ${item.wilayah}"
            binding.tvListBulan.text = item.listPeriode
            binding.tvUsername.text = "Telp: ${item.telepon}"
            binding.tvPaket.text = "" // Not provided in this API

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PelangganTelatTahunanItem>() {
        override fun areItemsTheSame(oldItem: PelangganTelatTahunanItem, newItem: PelangganTelatTahunanItem): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(oldItem: PelangganTelatTahunanItem, newItem: PelangganTelatTahunanItem): Boolean {
            return oldItem == newItem
        }
    }
}
