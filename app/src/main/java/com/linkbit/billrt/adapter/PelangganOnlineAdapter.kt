package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganOnlineBinding
import com.linkbit.billrt.PelangganOnline

class PelangganOnlineAdapter(
    private var pelangganList: List<PelangganOnline>,
    private val onKickClick: (PelangganOnline) -> Unit
) : RecyclerView.Adapter<PelangganOnlineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganOnlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount() = pelangganList.size

    fun updateData(newPelangganList: List<PelangganOnline>) {
        pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganOnlineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganOnline) {
            binding.tvName.text = pelanggan.name
            binding.tvAddress.text = pelanggan.address
            binding.tvUptime.text = pelanggan.uptime
            binding.btnKick.setOnClickListener {
                onKickClick(pelanggan)
            }
        }
    }
}
