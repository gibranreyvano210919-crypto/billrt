package com.linkbit.billrt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemOnuDetailBinding

class OnuAdapter(private var onuList: List<SmartOnuItem>) : RecyclerView.Adapter<OnuAdapter.OnuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnuViewHolder {
        val binding = ItemOnuDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnuViewHolder, position: Int) {
        holder.bind(onuList[position])
    }

    override fun getItemCount() = onuList.size

    fun updateData(newOnuList: List<SmartOnuItem>) {
        this.onuList = newOnuList
        notifyDataSetChanged()
    }

    inner class OnuViewHolder(private val binding: ItemOnuDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(onu: SmartOnuItem) {
            binding.tvOnuName.text = onu.name ?: "N/A"
            binding.tvOnuSn.text = "SN: ${onu.sn ?: "-"}"
            binding.tvOnuPower.text = "Power: ${onu.power ?: "-"}"
            binding.tvOltName.text = "(OLT: ${onu.oltName})"

            // Set status text and color
            when (val status = onu.status?.lowercase()) {
                "up" -> {
                    binding.tvOnuStatus.text = "UP"
                    binding.tvOnuStatus.setTextColor(Color.GREEN)
                }
                "down" -> {
                    binding.tvOnuStatus.text = "DOWN"
                    binding.tvOnuStatus.setTextColor(Color.RED)
                }
                "dying gasp" -> {
                    binding.tvOnuStatus.text = "POWERDOWN"
                    binding.tvOnuStatus.setTextColor(Color.parseColor("#FFA500")) // Orange
                }
                else -> {
                    binding.tvOnuStatus.text = status?.uppercase() ?: "UNKNOWN"
                    binding.tvOnuStatus.setTextColor(Color.GRAY)
                }
            }
        }
    }
}