package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemPelangganTelatBinding
import com.linkbit.billrt.model.PelangganTelatItem
import java.text.NumberFormat
import java.util.Locale

class PelangganTelatAdapter(
    private val onItemClick: (PelangganTelatItem) -> Unit,
    private val onItemLongClick: (PelangganTelatItem) -> Unit
) : ListAdapter<PelangganTelatItem, PelangganTelatAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganTelatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }
    }

    inner class ViewHolder(private val binding: ItemPelangganTelatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PelangganTelatItem) {
            val context = binding.root.context
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvUsername.text = item.mikrotikUsername
            binding.tvWilayah.text = item.wilayah
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvNominal.text = formatter.format(item.nominal).replace("Rp", "Rp ")

            binding.tvPeriodeTagihan.text = "Periode: ${item.periodeTagihan}"
            binding.tvTglJatuhTempo.text = "Jatuh Tempo: Tgl ${item.tglJatuhTempo}"
            binding.tvTglBayarTerakhir.text = "Bayar Terakhir: ${item.tglBayarTerakhir}"
            binding.tvPencatat.text = "Pencatat Terakhir: ${item.namaPencatat}"
            binding.tvKeterangan.text = item.keterangan
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PelangganTelatItem>() {
        override fun areItemsTheSame(oldItem: PelangganTelatItem, newItem: PelangganTelatItem): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(oldItem: PelangganTelatItem, newItem: PelangganTelatItem): Boolean {
            return oldItem == newItem
        }
    }
}
