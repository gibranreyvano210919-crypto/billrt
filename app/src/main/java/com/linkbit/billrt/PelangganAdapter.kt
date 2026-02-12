package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCardBinding

class PelangganAdapter(
    private var pelangganList: List<PelangganData>,
    private val onDetailClick: ((PelangganData) -> Unit)? = null,
    private val onMenuClick: ((PelangganData, View) -> Unit)? = null,
    private val onEditClick: ((PelangganData) -> Unit)? = null,
    private val onItemLongClick: ((PelangganData) -> Unit)? = null // New
) : RecyclerView.Adapter<PelangganAdapter.PelangganViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val binding = ItemPelangganCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PelangganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.bind(pelanggan)
    }

    override fun getItemCount(): Int = pelangganList.size

    fun updateData(newPelangganList: List<PelangganData>) {
        pelangganList = newPelangganList
        notifyDataSetChanged()
    }

    inner class PelangganViewHolder(private val binding: ItemPelangganCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganData) {
            binding.tvNamaPelanggan.text = pelanggan.nama
            binding.tvIdPelanggan.text = "ID: ${pelanggan.idPelanggan}"
            binding.tvAlamat.text = pelanggan.alamat
            binding.tvWilayah.text = "Wilayah: ${pelanggan.namaWilayah}"
            binding.tvStatusBerlangganan.text = "Status: ${pelanggan.tglDaftar}"

            if (onDetailClick != null) {
                binding.btnLihatDetail.visibility = View.VISIBLE
                binding.btnLihatDetail.setOnClickListener { onDetailClick.invoke(pelanggan) }
            } else {
                binding.btnLihatDetail.visibility = View.GONE
            }

            if (onMenuClick != null) {
                binding.btnMenuPelanggan.visibility = View.VISIBLE
                binding.btnMenuPelanggan.setOnClickListener { view ->
                    onMenuClick.invoke(pelanggan, view)
                }
            } else {
                binding.btnMenuPelanggan.visibility = View.GONE
            }

            if (onEditClick != null) {
                binding.btnEdit.visibility = View.VISIBLE
                binding.btnEdit.setOnClickListener { onEditClick.invoke(pelanggan) }
            } else {
                binding.btnEdit.visibility = View.GONE
            }

            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(pelanggan)
                true
            }
        }
    }
}
