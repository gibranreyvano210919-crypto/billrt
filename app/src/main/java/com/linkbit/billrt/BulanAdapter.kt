package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemBulanBinding
import java.io.Serializable

// This adapter is temporarily unused as there is no corresponding API endpoint.
data class BulanTagihanData(val nama: String, val statusTagihan: String, val statusLunas: Int, val totalBayar: Float, val tanggalBayar: String) : Serializable

class BulanAdapter(private val bulanList: List<BulanTagihanData>, private val onItemClick: (BulanTagihanData) -> Unit) : RecyclerView.Adapter<BulanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBulanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bulanList[position])
    }

    override fun getItemCount() = bulanList.size

    inner class ViewHolder(private val binding: ItemBulanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bulan: BulanTagihanData) {
            // UI Binding is disabled as the feature is not implemented in the API
        }
    }
}