package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemNotifikasiSettingBinding

class NotifikasiSettingAdapter(private val items: List<Notifikasi>) : RecyclerView.Adapter<NotifikasiSettingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifikasiSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemNotifikasiSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Notifikasi) {
            binding.tvNotificationTitle.text = item.title
            binding.tvNotificationMessage.text = item.message
        }
    }
}