package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPemasukanBinding
import com.linkbit.billrt.databinding.ItemPemasukanHeaderBinding
import com.linkbit.billrt.model.PemasukanGroup
import com.linkbit.billrt.model.PemasukanItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PemasukanAdapter(private var groups: List<PemasukanGroup>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val flatList = mutableListOf<Any>()
    private val expandedDates = mutableSetOf<String>()
    private val displayFormat = SimpleDateFormat("d MMM yyyy", Locale("in", "ID"))
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        flattenData()
    }

    fun updateData(newGroups: List<PemasukanGroup>) {
        groups = newGroups
        flattenData()
        notifyDataSetChanged()
    }

    private fun flattenData() {
        flatList.clear()
        groups.forEach { group ->
            flatList.add(group)
            if (expandedDates.contains(group.tanggalBayar)) {
                flatList.addAll(group.rincian)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (flatList[position] is PemasukanGroup) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemPemasukanHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemPemasukanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = flatList[position]
        if (holder is HeaderViewHolder && item is PemasukanGroup) {
            holder.bind(item)
        } else if (holder is ItemViewHolder && item is PemasukanItem) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = flatList.size

    inner class HeaderViewHolder(private val binding: ItemPemasukanHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: PemasukanGroup) {
            val date = try {
                val d = apiFormat.parse(group.tanggalBayar)
                if (d != null) displayFormat.format(d) else group.tanggalBayar
            } catch (e: Exception) {
                group.tanggalBayar
            }
            binding.tvTanggalHeader.text = date
            binding.tvJumlahPelanggan.text = group.jumlahPelanggan.toString()
            binding.tvTotalHeader.text = formatRupiah(group.totalPerTanggal)

            val isExpanded = expandedDates.contains(group.tanggalBayar)
            binding.ivExpand.rotation = if (isExpanded) 180f else 0f

            binding.root.setOnClickListener {
                if (isExpanded) {
                    expandedDates.remove(group.tanggalBayar)
                } else {
                    expandedDates.add(group.tanggalBayar)
                }
                flattenData()
                notifyDataSetChanged()
            }
        }
    }

    inner class ItemViewHolder(private val binding: ItemPemasukanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PemasukanItem) {
            binding.tvNamaPelanggan.text = "${item.noUrut}. ${item.namaPelanggan}"
            binding.tvJumlahBayar.text = formatRupiah(item.jumlahBayar)
            binding.tvIdTagihan.text = "#${item.idPembayaran}"
            binding.tvPeriode.text = "Periode: ${item.periodeTagihan}"
            
            binding.tvTanggalBayar.text = try {
                if (item.createdAt.contains(" ")) item.createdAt.split(" ")[1] else item.createdAt
            } catch (e: Exception) {
                item.createdAt
            }
            
            binding.tvNamaAdmin.text = "Admin: ${item.namaAdmin}"
            binding.tvMetode.visibility = View.GONE
            binding.tvKeterangan.visibility = View.GONE
        }
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace(",00", "")
    }
}
