package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganBelumBayarBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem

class PelangganBelumBayarAdapter(
    private val onItemClick: (PelangganBelumBayarItem) -> Unit
) : ListAdapter<PelangganBelumBayarItem, PelangganBelumBayarAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganBelumBayarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemPelangganBelumBayarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PelangganBelumBayarItem) {
            binding.tvNama.text = item.namaPelanggan
            binding.tvUsername.text = "Username: ${item.mikrotikUsername ?: "-"}"
            binding.tvTelepon.text = "Telepon: ${item.teleponPelanggan ?: "-"}"
            binding.tvWilayah.text = "Wilayah: ${item.wilayah ?: "-"}"
            binding.tvInvoice.text = "Invoice: ${item.invoice ?: "-"}"
            binding.tvNominal.text = "Nominal: Rp. ${item.nominal}"
            binding.tvPeriodeTagihan.text = "Periode: ${item.bulanTagihan}/${item.tahunTagihan}"
            binding.tvStatusTagihan.text = "Status: ${if (item.statusPembayaran == 0) "Belum Bayar" else "Lunas"}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PelangganBelumBayarItem>() {
        override fun areItemsTheSame(oldItem: PelangganBelumBayarItem, newItem: PelangganBelumBayarItem): Boolean {
            return oldItem.id_pelanggan == newItem.id_pelanggan
        }

        override fun areContentsTheSame(oldItem: PelangganBelumBayarItem, newItem: PelangganBelumBayarItem): Boolean {
            return oldItem == newItem
        }
    }
}
