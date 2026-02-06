package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemLaporanWilayahBinding

class LaporanWilayahAdapter(private var detailWilayah: List<DetailWilayah>) : RecyclerView.Adapter<LaporanWilayahAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaporanWilayahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(detailWilayah[position])
    }

    override fun getItemCount() = detailWilayah.size

    fun updateData(newDetailWilayah: List<DetailWilayah>) {
        detailWilayah = newDetailWilayah
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemLaporanWilayahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: DetailWilayah) {
            binding.tvNamaWilayah.text = detail.namaWilayah
            binding.tvTotalLunasWilayah.text = "Lunas: ${detail.lunas} / ${detail.total}"
        }
    }
}