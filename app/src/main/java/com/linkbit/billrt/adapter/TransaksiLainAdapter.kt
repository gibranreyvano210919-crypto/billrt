package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemTransaksiLainBinding
import com.linkbit.billrt.model.TransaksiLain
import java.text.NumberFormat
import java.util.Locale

class TransaksiLainAdapter : ListAdapter<TransaksiLain, TransaksiLainAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransaksiLainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTransaksiLainBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransaksiLain) {
            binding.apply {
                tvKategori.text = item.kategori
                tvTanggal.text = formatTanggal(item.tanggal)
                tvKeterangan.text = item.keterangan
                
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvJumlah.text = formatter.format(item.jumlah)
            }
        }

        private fun formatTanggal(rawDate: String?): String {
            if (rawDate == null) return "-"
            return try {
                // Menangani format: "Sat Aug 08 2026 00:00:00 GMT+0700 (Indochina Time)"
                // Mengambil bagian "08 Aug 2026" saja agar lebih rapi
                val parts = rawDate.split(" ")
                if (parts.size >= 4) {
                    "${parts[2]} ${parts[1]} ${parts[3]}"
                } else {
                    rawDate
                }
            } catch (e: Exception) {
                rawDate
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransaksiLain>() {
        override fun areItemsTheSame(oldItem: TransaksiLain, newItem: TransaksiLain): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransaksiLain, newItem: TransaksiLain): Boolean {
            return oldItem == newItem
        }
    }
}
