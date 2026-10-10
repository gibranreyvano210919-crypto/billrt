package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPengeluaranBinding
import com.linkbit.billrt.model.PengeluaranItem
import java.text.NumberFormat
import java.util.*

class PengeluaranAdapter(
    private val onItemClick: (PengeluaranItem) -> Unit
) : ListAdapter<PengeluaranItem, PengeluaranAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengeluaranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPengeluaranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PengeluaranItem) {
            binding.tvTanggal.text = item.tanggal
            binding.tvKategori.text = item.kategori
            binding.tvKeterangan.text = item.keterangan
            
            if (item.keteranganLain.isNotEmpty()) {
                binding.tvKeteranganLain.text = item.keteranganLain
                binding.tvKeteranganLain.visibility = View.VISIBLE
            } else {
                binding.tvKeteranganLain.visibility = View.GONE
            }

            binding.tvMetode.text = "Metode: ${item.metodePembayaran}"
            binding.tvJumlah.text = formatRupiah(item.jumlah)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "")
    }

    class DiffCallback : DiffUtil.ItemCallback<PengeluaranItem>() {
        override fun areItemsTheSame(oldItem: PengeluaranItem, newItem: PengeluaranItem): Boolean {
            return oldItem.idPengeluaran == newItem.idPengeluaran
        }

        override fun areContentsTheSame(oldItem: PengeluaranItem, newItem: PengeluaranItem): Boolean {
            return oldItem == newItem
        }
    }
}
