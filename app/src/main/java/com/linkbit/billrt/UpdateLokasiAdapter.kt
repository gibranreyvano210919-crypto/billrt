package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganUpdateLokasiBinding

class UpdateLokasiAdapter(
    private var pelangganList: List<PelangganData>,
    private val onItemClick: (PelangganData) -> Unit
) :
    RecyclerView.Adapter<UpdateLokasiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganUpdateLokasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount() = pelangganList.size

    fun updateList(newList: List<PelangganData>) {
        pelangganList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganUpdateLokasiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganData) {
            // Corrected: PelangganData uses 'nama', not 'namaPelanggan'
            binding.tvNamaPelangganUpdate.text = pelanggan.nama
            binding.tvIdPelangganUpdate.text = "ID: ${pelanggan.idPelanggan}"
            itemView.setOnClickListener { onItemClick(pelanggan) }
        }
    }
}