package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCardBinding

class PelangganNonaktifAdapter(
    private var pelangganList: List<PelangganNonaktif>,
    private val onDetailClick: (PelangganNonaktif) -> Unit,
    private val onMenuClick: ((PelangganNonaktif, View) -> Unit)? = null
) : RecyclerView.Adapter<PelangganNonaktifAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.bind(pelanggan)
    }

    override fun getItemCount(): Int = pelangganList.size

    fun updateData(newPelangganList: List<PelangganNonaktif>) {
        pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganNonaktif) {
            binding.tvNamaPelanggan.text = pelanggan.namaPelanggan
            binding.tvIdPelanggan.text = "ID: ${pelanggan.idPelanggan}"
            binding.tvAlamat.text = pelanggan.teleponPelanggan // Using address field to show phone
            binding.tvWilayah.text = "Wilayah: ${pelanggan.idWilayah}"
            binding.tvStatusBerlangganan.text = "Tgl Daftar: ${pelanggan.tglDaftar}"

            binding.btnLihatDetail.setOnClickListener { onDetailClick(pelanggan) }

            if (onMenuClick != null) {
                binding.btnMenuPelanggan.visibility = View.VISIBLE
                binding.btnMenuPelanggan.setOnClickListener { view ->
                    onMenuClick.invoke(pelanggan, view)
                }
            } else {
                binding.btnMenuPelanggan.visibility = View.GONE
            }
            
            binding.btnEdit.visibility = View.GONE
        }
    }
}
