package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemNotifikasiBinding

class NotificationAdapter(private var notificationList: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    // 1. Definisikan ViewHolder di dalam kelas Adapter
    inner class NotificationViewHolder(val binding: ItemNotifikasiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotifikasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notificationList[position]

        holder.binding.apply {
            // Pastikan ID di XML (item_notifikasi.xml) adalah tvNotificationTitle, tvNotificationBody, tvNotificationTime
            tvNotificationTitle.text = item.title
            tvNotificationBody.text = item.message  // Sesuai model NotificationItem kamu
            tvNotificationTime.text = item.time     // Sesuai model NotificationItem kamu
        }
    }

    override fun getItemCount(): Int = notificationList.size

    // Fungsi untuk update data dari Fragment
    fun updateData(newList: List<NotificationItem>) {
        notificationList.clear()
        notificationList.addAll(newList)
        notifyDataSetChanged()
    }
}