package com.linkbit.billrt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemSmartoltBinding

class OltServerAdapter(private var oltList: List<OltDetail>) : RecyclerView.Adapter<OltServerAdapter.OltViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OltViewHolder {
        val binding = ItemSmartoltBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OltViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OltViewHolder, position: Int) {
        holder.bind(oltList[position])
    }

    override fun getItemCount() = oltList.size

    fun updateData(newOltList: List<OltDetail>) {
        this.oltList = newOltList
        notifyDataSetChanged()
    }

    inner class OltViewHolder(private val binding: ItemSmartoltBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(olt: OltDetail) {
            // Corrected property name to match the data class
            binding.tvOltName.text = olt.olt_name 
            binding.tvOltIp.text = olt.address
            
            // Display status from OltDetail, or summary if status is null
            if (olt.status != null) {
                binding.tvOltSummary.text = "Status: ${olt.status}"
            } else {
                olt.summary?.let {
                    binding.tvOltSummary.text = "Total: ${it.total}, Online: ${it.online}, Offline: ${it.offline}"
                }
            }
        }
    }
}