package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.api.ListLunasTanggalItem
import com.linkbit.billrt.databinding.ItemStatementLunasTanggalBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class StatementLunasGroupedAdapter : ListAdapter<ListLunasTanggalItem, StatementLunasGroupedAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatementLunasTanggalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemStatementLunasTanggalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListLunasTanggalItem) {
            binding.tvNamaPelanggan.text = item.nama
            
            // Format waktu atau tanggal dari tanggal_bayar
            val timeDetail = try {
                if (item.tanggalBayar.contains(":")) {
                    // Jika ada format waktu (Y-m-d H:i:s)
                    val sdfIn = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val sdfOut = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = sdfIn.parse(item.tanggalBayar)
                    date?.let { "Jam: ${sdfOut.format(it)}" } ?: "-"
                } else {
                    // Jika hanya format tanggal (Y-m-d), tampilkan tanggalnya saja
                    val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val sdfOut = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val date = sdfIn.parse(item.tanggalBayar)
                    date?.let { "Tgl: ${sdfOut.format(it)}" } ?: "-"
                }
            } catch (e: Exception) {
                "-"
            }

            binding.tvDetail.text = "Periode: ${item.periode} • $timeDetail"
            binding.tvNamaPencatat.text = "Collector: ${item.namaPencatat ?: "System/Admin"}"
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }
            binding.tvJumlah.text = "+ ${formatter.format(item.nominal).replace("Rp", "Rp ")}"
            binding.tvMetode.text = item.metode
            binding.tvStatusBayar.text = item.statusPeriode
            
            // Atur warna status bayar
            if (item.statusPeriode.equals("Telat", ignoreCase = true)) {
                binding.tvStatusBayar.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
            } else if (item.statusPeriode.equals("Tepat", ignoreCase = true)) {
                binding.tvStatusBayar.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
            } else {
                binding.tvStatusBayar.setTextColor(binding.root.context.getColor(android.R.color.white))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ListLunasTanggalItem>() {
        override fun areItemsTheSame(oldItem: ListLunasTanggalItem, newItem: ListLunasTanggalItem): Boolean {
            return oldItem.nama == newItem.nama && oldItem.tanggalBayar == newItem.tanggalBayar
        }
        override fun areContentsTheSame(oldItem: ListLunasTanggalItem, newItem: ListLunasTanggalItem): Boolean = oldItem == newItem
    }
}
