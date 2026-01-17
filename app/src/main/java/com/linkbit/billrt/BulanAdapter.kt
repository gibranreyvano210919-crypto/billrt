package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemBulanBinding
import java.text.NumberFormat
import java.util.Locale

class BulanAdapter(
    private val bulanList: List<BulanTagihanData>,
    private val onItemClick: (BulanTagihanData) -> Unit
) : RecyclerView.Adapter<BulanAdapter.BulanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulanViewHolder {
        val binding = ItemBulanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BulanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BulanViewHolder, position: Int) {
        val bulan = bulanList[position]
        holder.bind(bulan)
        holder.itemView.setOnClickListener { onItemClick(bulan) }
    }

    override fun getItemCount() = bulanList.size

    class BulanViewHolder(private val binding: ItemBulanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bulan: BulanTagihanData) {
            binding.tvMonthName.text = bulan.nama
            binding.tvStatus.text = bulan.statusTagihan

            if (bulan.statusLunas) {
                binding.tvStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_lunas)
                binding.tvTotalBayar.visibility = View.VISIBLE
                binding.tvTanggalBayar.visibility = View.VISIBLE
                binding.tvTotalBayar.text = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(bulan.totalBayar)
                binding.tvTanggalBayar.text = "Dibayar pada: ${bulan.tanggalBayar}"
            } else {
                binding.tvStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.status_background_belum_lunas)
                binding.tvTotalBayar.visibility = View.GONE
                binding.tvTanggalBayar.visibility = View.GONE
            }
        }
    }
}
