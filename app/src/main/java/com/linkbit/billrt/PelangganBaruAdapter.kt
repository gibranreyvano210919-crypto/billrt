package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCardBinding
import com.linkbit.billrt.network.PelangganBaru

class PelangganBaruAdapter(
    private var pelangganBaruList: List<PelangganBaru>
) : RecyclerView.Adapter<PelangganBaruAdapter.PelangganBaruViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganBaruViewHolder {
        val binding = ItemPelangganCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PelangganBaruViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PelangganBaruViewHolder, position: Int) {
        holder.bind(pelangganBaruList[position])
    }

    override fun getItemCount() = pelangganBaruList.size

    fun updateData(newPelangganBaruList: List<PelangganBaru>) {
        pelangganBaruList = newPelangganBaruList
        notifyDataSetChanged()
    }

    inner class PelangganBaruViewHolder(private val binding: ItemPelangganCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganBaru) {
            binding.tvNamaPelanggan.text = pelanggan.namaPelanggan
            binding.tvIdPelanggan.text = pelanggan.idPelanggan
            binding.tvAlamat.text = pelanggan.namaWilayah
            binding.tvStatusBerlangganan.text = pelanggan.statusAktif
            
            // Hide unused buttons
            binding.btnMenuPelanggan.visibility = View.GONE
            binding.btnLihatDetail.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
        }
    }
}