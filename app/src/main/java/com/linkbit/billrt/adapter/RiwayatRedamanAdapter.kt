package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemRiwayatRedamanBinding
import com.linkbit.billrt.model.RiwayatRedamanItem

class RiwayatRedamanAdapter(private var itemList: List<RiwayatRedamanItem>) : RecyclerView.Adapter<RiwayatRedamanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatRedamanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    fun updateData(newItemList: List<RiwayatRedamanItem>) {
        itemList = newItemList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRiwayatRedamanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatRedamanItem) {
            binding.tvSignalValue.text = item.signal
            binding.tvTimestamp.text = item.timestamp
        }
    }
}
