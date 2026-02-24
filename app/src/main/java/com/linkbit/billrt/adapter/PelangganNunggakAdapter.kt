package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganNunggakBinding
import com.linkbit.billrt.model.PelangganNunggakItem

class PelangganNunggakAdapter(
    private val onItemClick: (PelangganNunggakItem) -> Unit
) : ListAdapter<PelangganNunggakItem, PelangganNunggakAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganNunggakBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemPelangganNunggakBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PelangganNunggakItem) {
            binding.tvNamaPelanggan.text = item.nama_pelanggan
            binding.tvWilayah.text = item.nama_wilayah
            binding.tvUsername.text = item.mikrotik_username
            binding.tvTotalPiutang.text = "Rp ${item.total_tunggakan.toInt()}"
            binding.tvInstallationDate.text = item.installation_date
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PelangganNunggakItem>() {
        override fun areItemsTheSame(oldItem: PelangganNunggakItem, newItem: PelangganNunggakItem): Boolean {
            return oldItem.id_pelanggan == newItem.id_pelanggan
        }

        override fun areContentsTheSame(oldItem: PelangganNunggakItem, newItem: PelangganNunggakItem): Boolean {
            return oldItem == newItem
        }
    }
}
