package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganOfflineBinding
import com.linkbit.billrt.PelangganOffline // DIUBAH: Import dari root package

class PelangganOfflineAdapter(
    private var pelangganList: List<PelangganOffline>
) : RecyclerView.Adapter<PelangganOfflineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganOfflineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount() = pelangganList.size

    fun updateData(newPelangganList: List<PelangganOffline>) {
        pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganOfflineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganOffline) {
            binding.tvName.text = pelanggan.name
            binding.tvLastLoggedOut.text = pelanggan.lastLoggedOut ?: "-"
            binding.tvComment.text = pelanggan.comment
        }
    }
}
