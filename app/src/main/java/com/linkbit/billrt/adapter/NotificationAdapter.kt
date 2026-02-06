package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.NotificationItem
import com.linkbit.billrt.databinding.ItemNotifikasiBinding

class NotificationAdapter(private var items: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNotifikasiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifikasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvNotificationTitle.text = item.title
            tvNotificationBody.text = item.message
            tvNotificationTime.text = item.time
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<NotificationItem>) {
        val diffCallback = NotificationDiffCallback(this.items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.items.clear()
        this.items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }
}

class NotificationDiffCallback(
    private val oldList: List<NotificationItem>,
    private val newList: List<NotificationItem>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int) = 
        oldList[oldPos].time == newList[newPos].time && oldList[oldPos].title == newList[newPos].title

    override fun areContentsTheSame(oldPos: Int, newPos: Int) = 
        oldList[oldPos] == newList[newPos]
}
