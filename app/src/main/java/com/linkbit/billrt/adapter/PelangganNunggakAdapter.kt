package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganNunggakBinding
import com.linkbit.billrt.model.PelangganNunggakItem
import java.text.NumberFormat
import java.util.Locale

class PelangganNunggakAdapter(
    private val onItemClick: (PelangganNunggakItem) -> Unit
) : ListAdapter<PelangganNunggakItem, PelangganNunggakAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganNunggakBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    inner class ViewHolder(private val binding: ItemPelangganNunggakBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        fun bind(item: PelangganNunggakItem) {
            binding.tvNamaPelanggan.text = item.nama_pelanggan
            binding.tvWilayah.text = "Wilayah: ${item.nama_wilayah}"
            binding.tvUsername.text = "User: ${item.mikrotik_username}"
            binding.tvTotalPiutang.text = formatter.format(item.total_tunggakan)
            binding.tvJatuhTempo.text = "Jatuh Tempo: Tgl ${item.jatuh_tempo_tgl}"
            binding.tvRiwayatBayar.text = "Terakhir Bayar: ${item.tgl_bayar_terakhir}"
            binding.tvPeriode.text = "Periode: ${item.periode_nunggak}"
            binding.tvAdmin.text = "Admin: ${item.admin_pencatat}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PelangganNunggakItem>() {
        override fun areItemsTheSame(oldItem: PelangganNunggakItem, newItem: PelangganNunggakItem): Boolean {
            return oldItem.id_pelanggan == newItem.id_pelanggan
        }

        override fun areContentsTheSame(oldItem: PelangganNunggakItem, newItem: PelangganNunggakItem): Boolean {
            return oldItem == newItem
        }
    }
}
