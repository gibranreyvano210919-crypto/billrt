package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemJadwalTagihanBinding
import com.linkbit.billrt.model.JadwalTagihanItem

class JadwalTagihanAdapter(
    private val onItemClick: (JadwalTagihanItem) -> Unit
) : ListAdapter<JadwalTagihanItem, JadwalTagihanAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJadwalTagihanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemJadwalTagihanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: JadwalTagihanItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvWilayah.text = item.wilayah
            binding.tvJatuhTempo.text = "Jatuh Tempo: Tgl ${item.tglJatuhTempo}"
            binding.tvNominal.text = "Rp ${item.nominal.toInt()}"
            binding.tvBayarTerakhir.text = "Terakhir Bayar: ${item.tglTerakhirBayar}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<JadwalTagihanItem>() {
        override fun areItemsTheSame(oldItem: JadwalTagihanItem, newItem: JadwalTagihanItem): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(oldItem: JadwalTagihanItem, newItem: JadwalTagihanItem): Boolean {
            return oldItem == newItem
        }
    }
}
