package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemPelangganLunasBinding
import com.linkbit.billrt.model.PelangganLunasItem
import java.text.NumberFormat
import java.util.Locale

class LunasAdapter(private val onClick: (PelangganLunasItem) -> Unit) : ListAdapter<PelangganLunasItem, LunasAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganLunasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { 
            onClick(item)
        }
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemPelangganLunasBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: PelangganLunasItem) {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
                maximumFractionDigits = 0
            }

            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvUsername.text = "Username: ${item.mikrotikUsername ?: "-"}"
            binding.tvWilayah.text = "Wilayah: ${item.wilayah ?: "-"}"
            binding.tvNominal.text = numberFormat.format(item.nominalTagihan)
            binding.tvTanggalBayar.text = item.tanggalBayar ?: "-"
            binding.tvPencatat.text = "Diterima oleh: ${item.namaPencatat ?: "Sistem"}"
            binding.tvKeterangan.text = "Ket: ${item.keterangan?.takeIf { it.isNotBlank() } ?: "-"}"

            val monthNames = arrayOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
            val monthName = if (item.bulanTagihan in 1..12) monthNames[item.bulanTagihan - 1] else "-"
            binding.tvPeriode.text = "Periode: $monthName ${item.tahunTagihan}"

            // Use context to get color safely
            val primaryColor = ContextCompat.getColor(itemView.context, R.color.card_pelanggan)
            binding.tvNominal.setTextColor(primaryColor)

            binding.badgeTelat.visibility = android.view.View.GONE
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PelangganLunasItem>() {
        override fun areItemsTheSame(oldItem: PelangganLunasItem, newItem: PelangganLunasItem): Boolean {
            return oldItem.idTagihan == newItem.idTagihan
        }

        override fun areContentsTheSame(oldItem: PelangganLunasItem, newItem: PelangganLunasItem): Boolean {
            return oldItem == newItem
        }
    }
}
