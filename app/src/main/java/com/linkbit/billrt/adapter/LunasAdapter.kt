package com.linkbit.billrt.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemPelangganLunasBinding
import com.linkbit.billrt.model.PelangganLunasItem

class LunasAdapter(private val onClick: (PelangganLunasItem) -> Unit) : ListAdapter<PelangganLunasItem, LunasAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganLunasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { 
            onClick(item)
        }
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemPelangganLunasBinding) : RecyclerView.ViewHolder(binding.root) {
        private val defaultTanggalBayarBackground: Drawable? = binding.tvTanggalBayar.background
        private val defaultTanggalBayarTextColor: ColorStateList = binding.tvTanggalBayar.textColors

        fun bind(item: PelangganLunasItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan.replace(" (Telat)", "")
            binding.tvUsername.text = "Username: ${item.mikrotikUsername ?: "-"}"
            binding.tvNominal.text = "Nominal: Rp. ${item.nominalTagihan}"
            binding.tvTanggalBayar.text = "Tanggal Bayar: ${item.tanggalBayar ?: "-"}"
            binding.tvKeterangan.text = "Keterangan: ${item.keterangan?.replace(" (Telat)", "") ?: "-"}"

            if (item.isTelat) {
                binding.badgeTelat.visibility = View.VISIBLE
                binding.tvTanggalBayar.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.error))
                binding.tvTanggalBayar.setTextColor(Color.WHITE)
            } else {
                binding.badgeTelat.visibility = View.GONE
                binding.tvTanggalBayar.background = defaultTanggalBayarBackground
                binding.tvTanggalBayar.setTextColor(defaultTanggalBayarTextColor)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PelangganLunasItem>() {
        override fun areItemsTheSame(oldItem: PelangganLunasItem, newItem: PelangganLunasItem): Boolean {
            return oldItem.idTagihan == newItem.idTagihan
        }

        override fun areContentsTheSame(oldItem: PelangganLunasItem, newItem: PelangganLunasItem): Boolean {
            return oldItem == newItem
        }
    }
}
