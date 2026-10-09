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
        holder.bind(timelineItems[position], position == timelineItems.size - 1)
    }

    override fun getItemCount(): Int = timelineItems.size

    fun updateData(newItems: List<TimelineItem>) {
        timelineItems = newItems
        notifyDataSetChanged()
    }

    inner class TimelineViewHolder(private val binding: ItemTimelineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimelineItem, isLast: Boolean) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.visibility = View.GONE
            binding.tvDate.text = item.dateDisplay
            binding.tvStatus.text = item.statusText
            binding.tvDescription.visibility = View.GONE

            if (!item.adminPencatat.isNullOrEmpty()) {
                binding.tvAdmin.text = "Pencatat: ${item.adminPencatat}"
                binding.tvAdmin.visibility = View.VISIBLE
            } else {
                binding.tvAdmin.visibility = View.GONE
            }

            // Sembunyikan garis timeline vertikal dan divider horizontal untuk item terakhir
            binding.line.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.dividerLine.visibility = if (isLast) View.GONE else View.VISIBLE

            var statusColor = when {
                item.statusText.contains("Lunas", ignoreCase = true) -> Color.parseColor("#4CAF50") // Hijau
                item.statusText.contains("Tagout", ignoreCase = true) -> Color.parseColor("#FF9800") // Orange
                else -> Color.parseColor("#F44336") // Merah
            }
            try {
                if (!item.dotColor.isNullOrEmpty()) {
                    statusColor = Color.parseColor(item.dotColor)
                }
            } catch (e: Exception) {
                // Ignore parsing error
            }

            val dotBg = binding.dot.background?.mutate()
            if (dotBg is GradientDrawable) {
                dotBg.setColor(statusColor)
            } else {
                binding.dot.setBackgroundColor(statusColor)
            }

            val statusBg = binding.tvStatus.background?.mutate()
            if (statusBg is GradientDrawable) {
                statusBg.setColor(statusColor)
            } else {
                binding.tvStatus.setBackgroundColor(statusColor)
            }

            binding.tvStatus.visibility = View.VISIBLE
        }
    }
}
