package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCardBinding

class PelangganIsolirAdapter(
    private var pelangganList: List<PelangganIsolir>,
    private val onDetailClick: (PelangganIsolir) -> Unit,
    private val onLongClick: (PelangganIsolir) -> Unit
) : RecyclerView.Adapter<PelangganIsolirAdapter.PelangganViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val binding = ItemPelangganCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PelangganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.bind(pelanggan)
        holder.itemView.setOnClickListener { onDetailClick(pelanggan) }
        holder.itemView.setOnLongClickListener {
            onLongClick(pelanggan)
            true
        }
    }

    override fun getItemCount() = pelangganList.size

    fun updateData(newData: List<PelangganIsolir>) {
        pelangganList = newData
        notifyDataSetChanged()
    }

    inner class PelangganViewHolder(val binding: ItemPelangganCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganIsolir) {
            binding.tvNamaPelanggan.text = pelanggan.namaPelanggan
            binding.tvIdPelanggan.text = pelanggan.idPelanggan
            binding.tvStatusBerlangganan.text = "Isolir"

            // Hide unused buttons
            binding.btnLihatDetail.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnMenuPelanggan.visibility = View.GONE
        }
    }
}
