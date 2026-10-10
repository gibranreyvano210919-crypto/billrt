package com.linkbit.billrt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMonitoringOltBinding

class OltMonitorAdapter(
    private var list: List<OltAccount>,
    private val onItemClick: (OltAccount) -> Unit
) : RecyclerView.Adapter<OltMonitorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMonitoringOltBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMonitoringOltBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvNamaOlt.text = item.namaOlt
            tvHost.text = item.fullAddress
            tvTipe.text = item.tipeLabel
            tvLastSync.text = item.lastSync
            tvKeterangan.text = item.keterangan
            tvStatus.text = item.status.uppercase()

            if (item.status.equals("Online", ignoreCase = true)) {
                statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"))
                tvStatus.setBackgroundResource(R.drawable.bg_status_online)
            } else {
                statusIndicator.setBackgroundColor(Color.parseColor("#F44336"))
                tvStatus.setBackgroundResource(R.drawable.bg_status_offline)
            }

            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<OltAccount>) {
        list = newList
        notifyDataSetChanged()
    }
}
