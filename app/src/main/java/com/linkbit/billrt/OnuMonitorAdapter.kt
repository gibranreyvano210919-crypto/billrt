package com.linkbit.billrt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemOnuMonitorBinding

class OnuMonitorAdapter(private var list: List<OnuItemDetail>) : RecyclerView.Adapter<OnuMonitorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOnuMonitorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOnuMonitorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvOnuName.text = item.nama
            tvOnuMac.text = "MAC: ${item.mac}"
            tvPonPort.text = "PON ${item.ponPort}"
            tvOnuStatus.text = item.status.uppercase()
            tvOnuSignal.text = "${item.signal} dBm"
            tvKategori.text = item.kategori

            // Status Color
            if (item.status.equals("Online", ignoreCase = true) || item.status.equals("Up", ignoreCase = true)) {
                tvOnuStatus.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                tvOnuStatus.setTextColor(Color.parseColor("#F44336"))
            }

            // Signal/Category Color
            if (item.kategori.equals("Bahaya", ignoreCase = true)) {
                tvKategori.setBackgroundResource(R.drawable.bg_status_unpaid) // Assuming red bg exists
                tvKategori.setTextColor(Color.WHITE)
            } else {
                tvKategori.setBackgroundResource(R.drawable.bg_circle_green_light)
                tvKategori.setTextColor(Color.parseColor("#4CAF50"))
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<OnuItemDetail>) {
        list = newList
        notifyDataSetChanged()
    }
}
