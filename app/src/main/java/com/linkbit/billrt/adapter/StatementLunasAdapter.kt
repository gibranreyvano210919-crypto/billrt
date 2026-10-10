package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.api.ListLunasItem
import com.linkbit.billrt.databinding.ItemStatementLunasBinding
import java.text.NumberFormat
import java.util.Locale

class StatementLunasAdapter : ListAdapter<ListLunasItem, StatementLunasAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatementLunasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemStatementLunasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListLunasItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvDetail.text = "Periode: ${item.periode} • ${item.tanggalBayar ?: "-"}"
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }
            binding.tvJumlah.text = "+ ${formatter.format(item.jumlah)}"
            binding.tvMetode.text = item.metode ?: "Cash"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ListLunasItem>() {
        override fun areItemsTheSame(oldItem: ListLunasItem, newItem: ListLunasItem): Boolean {
            return oldItem.namaPelanggan == newItem.namaPelanggan && oldItem.tanggalBayar == newItem.tanggalBayar
        }
        override fun areContentsTheSame(oldItem: ListLunasItem, newItem: ListLunasItem): Boolean = oldItem == newItem
    }
}
