package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganTelatBinding
import com.linkbit.billrt.model.PelangganTelatItem

class PelangganTelatAdapter(
    private val onItemClick: (PelangganTelatItem) -> Unit
) : ListAdapter<PelangganTelatItem, PelangganTelatAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganTelatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemPelangganTelatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PelangganTelatItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvWilayah.text = item.wilayah
            binding.tvPeriodeTagihan.text = "Periode: ${item.periodeTagihan}"
            binding.tvUsername.text = item.mikrotikUsername
            binding.tvNominal.text = "Rp ${item.nominal.toInt()}"
            binding.tvTglTagih.text = "Tgl Tagih: ${item.tglTagih}"
            binding.tvTglBayarTerakhir.text = "Tgl Bayar Terakhir: ${item.tglBayarTerakhir ?: "-"}"
            binding.tvPeriodeBayarTerakhir.text = "Periode Bayar Terakhir: ${item.periodeBayarTerakhir ?: "-"}"
            binding.tvKeterangan.text = item.keterangan
            binding.tvKeterangan2.text = item.keterangan2
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PelangganTelatItem>() {
        override fun areItemsTheSame(oldItem: PelangganTelatItem, newItem: PelangganTelatItem): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(oldItem: PelangganTelatItem, newItem: PelangganTelatItem): Boolean {
            return oldItem == newItem
        }
    }
}
