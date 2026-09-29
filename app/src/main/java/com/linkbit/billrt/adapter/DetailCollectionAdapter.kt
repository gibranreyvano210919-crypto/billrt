package com.linkbit.billrt.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.api.ListPencatatDetailItem
import com.linkbit.billrt.databinding.ItemRekapWilayahHeaderBinding
import com.linkbit.billrt.databinding.ItemStatementLunasTanggalBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class DetailListItem {
    data class Header(
        val wilayah: String,
        val totalPelanggan: Int,
        val totalNominal: Double,
        val baru: Int,
        val lama: Int
    ) : DetailListItem()
    data class Item(val detail: ListPencatatDetailItem) : DetailListItem()
}

class DetailCollectionAdapter : ListAdapter<DetailListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DetailListItem.Header -> TYPE_HEADER
            is DetailListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemRekapWilayahHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemStatementLunasTanggalBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as DetailListItem.Header)
            is ItemViewHolder -> holder.bind((item as DetailListItem.Item).detail)
        }
    }

    class HeaderViewHolder(private val binding: ItemRekapWilayahHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: DetailListItem.Header) {
            binding.root.setBackgroundColor(Color.TRANSPARENT)
            // Fix: ViewBinding converts snake_case IDs to camelCase
            binding.tvNamaWilayahHeader.text = header.wilayah
            binding.tvNamaWilayahHeader.setTextColor(Color.WHITE)
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }
            val nominalStr = formatter.format(header.totalNominal).replace("Rp", "Rp ")
            
            binding.tvJumlahPelangganHeader.text = "${header.totalPelanggan} Pelanggan • $nominalStr"
            binding.tvJumlahPelangganHeader.setTextColor(Color.parseColor("#BDBDBD")) // grey_400
        }
    }

    class ItemViewHolder(private val binding: ItemStatementLunasTanggalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListPencatatDetailItem) {
            binding.tvNamaPelanggan.text = item.nama
            
            val status = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val payDate = sdf.parse(item.tanggalBayar)
                val calPay = Calendar.getInstance().apply { time = payDate!! }

                val parts = item.periodeTagihan.split("/")
                val targetMonth = parts[0].toInt()
                val targetYear = parts[1].toInt()

                val payYear = calPay.get(Calendar.YEAR)
                val payMonth = calPay.get(Calendar.MONTH) + 1

                if (payYear > targetYear || (payYear == targetYear && payMonth > targetMonth)) {
                    "Telat"
                } else {
                    "Tepat"
                }
            } catch (e: Exception) {
                "Tepat"
            }

            val tglFormat = try {
                val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfOut = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val date = sdfIn.parse(item.tanggalBayar)
                date?.let { sdfOut.format(it) } ?: item.tanggalBayar
            } catch (e: Exception) {
                item.tanggalBayar
            }

            binding.tvDetail.text = "Periode: ${item.periodeTagihan} • $tglFormat"
            
            binding.tvNamaPencatat.text = item.indikator ?: ""
            binding.tvNamaPencatat.setTextColor(binding.root.context.getColor(R.color.purple_500))
            
            binding.tvStatusBayar.text = status
            if (status == "Telat") {
                binding.tvStatusBayar.setTextColor(Color.RED)
            } else {
                binding.tvStatusBayar.setTextColor(Color.GREEN)
            }

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }
            binding.tvJumlah.text = "+ ${formatter.format(item.nominal).replace("Rp", "Rp ")}"
            binding.tvMetode.text = item.metode
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        val DiffCallback = object : DiffUtil.ItemCallback<DetailListItem>() {
            override fun areItemsTheSame(oldItem: DetailListItem, newItem: DetailListItem): Boolean {
                return if (oldItem is DetailListItem.Header && newItem is DetailListItem.Header) {
                    oldItem.wilayah == newItem.wilayah
                } else if (oldItem is DetailListItem.Item && newItem is DetailListItem.Item) {
                    oldItem.detail.nama == newItem.detail.nama && oldItem.detail.tanggalBayar == newItem.detail.tanggalBayar
                } else false
            }

            override fun areContentsTheSame(oldItem: DetailListItem, newItem: DetailListItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
