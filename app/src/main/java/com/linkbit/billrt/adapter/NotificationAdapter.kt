package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.NotificationItem
import com.linkbit.billrt.databinding.ItemNotifikasiBinding

class NotificationAdapter : ListAdapter<NotificationItem, NotificationAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemNotifikasiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifikasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            tvNotificationTitle.text = item.title
            tvNotificationBody.text = item.message
            tvNotificationTime.text = item.time
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            // Menggunakan time dan title sebagai identifier unik sederhana jika tidak ada ID
            return oldItem.time == newItem.time && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}
