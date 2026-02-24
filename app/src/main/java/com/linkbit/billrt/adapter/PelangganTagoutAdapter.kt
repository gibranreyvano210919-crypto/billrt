package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.TagoutItem
import com.linkbit.billrt.databinding.ItemPelangganTagoutBinding

class PelangganTagoutAdapter(
    private val onItemClick: (TagoutItem) -> Unit
) : ListAdapter<TagoutItem, PelangganTagoutAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganTagoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemPelangganTagoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TagoutItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvIdPelanggan.text = "#${item.idPelanggan}"
            binding.tvPeriodeTagihan.text = ": ${item.bulanTagihan}/${item.tahunTagihan}"
            binding.tvCatatanTagout.text = ": ${item.catatanTagout ?: "-"}"
            binding.tvUpdatedAt.text = ": ${item.updatedAt}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TagoutItem>() {
        override fun areItemsTheSame(oldItem: TagoutItem, newItem: TagoutItem): Boolean {
            return oldItem.idTagihan == newItem.idTagihan
        }

        override fun areContentsTheSame(oldItem: TagoutItem, newItem: TagoutItem): Boolean {
            return oldItem == newItem
        }
    }
}
