package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganTunggakanBinding
import com.linkbit.billrt.model.TunggakanItem

class TunggakanKumulatifAdapter(
    private val onItemClick: (TunggakanItem) -> Unit
) : ListAdapter<TunggakanItem, TunggakanKumulatifAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganTunggakanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemPelangganTunggakanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TunggakanItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvUsername.text = "${item.username} | ${item.wilayah}"
            // Note: If you want to show more info like totalNominal, you might need to update item_pelanggan_tunggakan.xml
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TunggakanItem>() {
        override fun areItemsTheSame(oldItem: TunggakanItem, newItem: TunggakanItem): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(oldItem: TunggakanItem, newItem: TunggakanItem): Boolean {
            return oldItem == newItem
        }
    }
}
