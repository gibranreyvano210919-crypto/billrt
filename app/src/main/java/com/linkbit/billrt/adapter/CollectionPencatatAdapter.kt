package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.api.ListPencatatItem
import com.linkbit.billrt.databinding.ItemStatementLunasBinding
import java.text.NumberFormat
import java.util.Locale

class CollectionPencatatAdapter(
    private val onItemClick: (ListPencatatItem) -> Unit
) : ListAdapter<ListPencatatItem, CollectionPencatatAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatementLunasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    class ViewHolder(private val binding: ItemStatementLunasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListPencatatItem) {
            binding.tvNamaPelanggan.text = item.namaPencatat
            binding.tvDetail.text = "${item.totalTransaksi} Transaksi • ${item.totalPelanggan} Pelanggan"
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }
            binding.tvJumlah.text = formatter.format(item.totalNominal).replace("Rp", "Rp ")
            binding.tvMetode.text = "Collector"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ListPencatatItem>() {
        override fun areItemsTheSame(oldItem: ListPencatatItem, newItem: ListPencatatItem): Boolean {
            return oldItem.idUserPencatat == newItem.idUserPencatat
        }
        override fun areContentsTheSame(oldItem: ListPencatatItem, newItem: ListPencatatItem): Boolean = oldItem == newItem
    }
}
