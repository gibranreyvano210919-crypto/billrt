package com.linkbit.billrt.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganBelumBayarBinding
import com.linkbit.billrt.model.PelangganBelumBayarItem
import java.text.NumberFormat
import java.util.Locale

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
            binding.tvNominal.text = "Nominal: ${formatCurrency(item.nominal)}"
            binding.tvPeriodeTagihan.text = "Periode: ${item.bulanTagihan} ${item.tahunTagihan}"
            
            // Tampilkan Performa Pembayaran
            binding.tvPerformaPembayaran.text = "Performa Bayar: ${item.performaPembayaran ?: "0%"}"
            
            // Set warna teks performa berdasarkan nilai
            val performa = item.performaFloat ?: 0f
            when {
                performa >= 80 -> binding.tvPerformaPembayaran.setTextColor(Color.parseColor("#4CAF50")) // Hijau
                performa >= 50 -> binding.tvPerformaPembayaran.setTextColor(Color.parseColor("#FF9800")) // Orange
                else -> binding.tvPerformaPembayaran.setTextColor(Color.parseColor("#F44336")) // Merah
            }
            
            binding.tvStatusTagihan.text = item.statusText ?: "Belum Bayar"
            // Set warna status jika mengandung kata "LUNAS"
            if (item.statusText?.contains("LUNAS", ignoreCase = true) == true) {
                binding.tvStatusTagihan.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                binding.tvStatusTagihan.setTextColor(Color.parseColor("#F44336"))
            }
            
            binding.tvPembayaranTerakhir.text = "Bayar Terakhir: ${item.tglBayarTerakhir ?: "-"}"
            binding.tvPencatat.text = "Oleh: ${item.namaPencatat ?: "-"}"
        }
    }

    private fun formatCurrency(amount: Float): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        try {
            val formatted = format.format(amount.toDouble())
            return formatted.replace("Rp", "Rp ").replace(",00", ",-")
        } catch (e: Exception) {
            return "Rp 0,-"
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
