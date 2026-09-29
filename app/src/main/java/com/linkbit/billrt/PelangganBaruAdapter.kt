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
            binding.tvIdPelanggan.text = "ID: ${pelanggan.idPelanggan}"
            
            // Menampilkan Tanggal Instalasi (installation_date) sesuai output JSON
            binding.tvAlamat.text = "Tgl Pasang: ${pelanggan.installationDate ?: "-"}"
            
            // Menampilkan Nama Wilayah (nama_wilayah) sesuai output JSON
            binding.tvWilayah.text = "Wilayah: ${pelanggan.namaWilayah ?: "Tanpa Wilayah"}"
            
            // Menampilkan Status Aktif (status_aktif)
            binding.tvStatusBerlangganan.text = "Status: ${pelanggan.statusAktif}"
            
            // Sembunyikan tombol yang tidak digunakan di list pelanggan baru
            binding.btnMenuPelanggan.visibility = View.GONE
            binding.btnLihatDetail.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
        }
    }
}