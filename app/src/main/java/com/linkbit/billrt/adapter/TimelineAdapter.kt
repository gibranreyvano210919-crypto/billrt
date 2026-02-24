package com.linkbit.billrt.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemTimelineBinding
import com.linkbit.billrt.model.TimelineItem

class TimelineAdapter(
    private var timelineItems: List<TimelineItem>
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(timelineItems[position])
    }

    override fun getItemCount(): Int = timelineItems.size

    fun updateData(newItems: List<TimelineItem>) {
        timelineItems = newItems
        notifyDataSetChanged()
    }

    inner class TimelineViewHolder(private val binding: ItemTimelineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimelineItem) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.tvDate.text = item.dateDisplay
            binding.tvStatus.text = item.statusText
            binding.tvDescription.text = item.description

            var statusColor = Color.GRAY
            try {
                statusColor = Color.parseColor(item.dotColor)
            } catch (e: IllegalArgumentException) {
                // Parsing error, use default gray
            }

            binding.dot.setBackgroundColor(statusColor)
            val statusBackground = binding.tvStatus.background
            if (statusBackground is GradientDrawable) {
                statusBackground.setColor(statusColor)
            }

            binding.tvStatus.visibility = View.VISIBLE
        }
    }
}