package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMonitoringOfflineBinding

class MonitoringAdapter(private var items: List<String>) : RecyclerView.Adapter<MonitoringAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMonitoringOfflineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMonitoringOfflineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvPelangganName.text = items[position]
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}
