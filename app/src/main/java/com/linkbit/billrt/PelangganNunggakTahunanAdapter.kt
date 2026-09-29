package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemNunggakTahunanBinding

class PelangganNunggakTahunanAdapter(
    private var items: List<NunggakTahunanItem>,
    private val onItemClick: (NunggakTahunanItem) -> Unit
) : RecyclerView.Adapter<PelangganNunggakTahunanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemNunggakTahunanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NunggakTahunanItem) {
            binding.tvNamaPelanggan.text = item.nama
            binding.tvJumlahBulan.text = "${item.jumlahBulan} Bulan"
            binding.tvWilayah.text = "Wilayah: ${item.wilayah ?: "-"}"
            binding.tvPaket.text = "Paket: ${item.paket ?: "-"}"
            binding.tvListBulan.text = item.listBulan ?: "-"
            binding.tvUsername.text = "User: ${item.username ?: "-"}"

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNunggakTahunanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NunggakTahunanItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
