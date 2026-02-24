package com.linkbit.billrt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemRiwayatCatatanBinding

class RiwayatCatatanAdapter(
    private var items: List<CatatanKasItem>
) : RecyclerView.Adapter<RiwayatCatatanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatCatatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<CatatanKasItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRiwayatCatatanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CatatanKasItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvMikrotikUsername.text = "(${item.mikrotikUsername ?: "-"})"
            binding.tvWilayah.text = "Wilayah: ${item.wilayah ?: "-"}"
            binding.tvTanggalCatat.text = item.tanggalCatat
            binding.tvNamaTeknisi.text = "Teknisi: ${item.namaTeknisi ?: "N/A"}"

            val context = binding.root.context
            if (item.isDuplicate) {
                binding.cardRiwayatItem.background = ContextCompat.getDrawable(context, R.drawable.duplicate_background)
            } else {
                // Menggunakan warna default atau putih
                binding.cardRiwayatItem.setCardBackgroundColor(Color.WHITE)
            }

            // Coba format tanggal jika activity adalah MainActivity
            (context as? MainActivity)?.let {
                binding.tvTanggalCatat.text = it.formatDate(item.tanggalCatat)
            }
        }
    }
}
