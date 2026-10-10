package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCardBinding

class PelangganNonaktifAdapter(
    private var pelangganList: List<PelangganNonaktif>,
    private val onDetailClick: (PelangganNonaktif) -> Unit,
    private val onItemLongClick: (PelangganNonaktif) -> Unit
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
            binding.tvAlamat.text = "Alamat: ${pelanggan.alamat ?: "-"}"
            binding.tvWilayah.text = "Wilayah: ${pelanggan.namaWilayah ?: "-"}"
            binding.tvStatusBerlangganan.text = "Status: ${pelanggan.statusAktif ?: "Nonaktif"} | PPPoE: ${pelanggan.mikrotikUsername ?: "-"} | Telp: ${pelanggan.teleponPelanggan ?: "-"}"

            binding.root.setOnClickListener { onDetailClick(pelanggan) }
            binding.root.setOnLongClickListener {
                onItemLongClick(pelanggan)
                true
            }
            
            binding.btnLihatDetail.setOnClickListener { onDetailClick(pelanggan) }
            binding.btnMenuPelanggan.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
        }
    }
}
