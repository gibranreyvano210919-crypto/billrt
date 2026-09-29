package com.linkbit.billrt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemCatatanKasBinding
import com.linkbit.billrt.databinding.ItemHeaderTanggalKasBinding
import java.text.NumberFormat
import java.util.Locale

sealed class RiwayatKasListItem {
    data class Header(val group: TanggalGroup) : RiwayatKasListItem()
    data class Item(val catatan: CatatanKasItem) : RiwayatKasListItem()
}

class RiwayatKasGroupedAdapter(
    private var items: List<RiwayatKasListItem>,
    private val onPrintClick: (String) -> Unit, // Changed: now passes a groupKey
    private val onDeleteClick: (CatatanKasItem) -> Unit,
    private val onItemClick: (CatatanKasItem) -> Unit,
    private val onSelectAllClick: (String) -> Unit, // Changed: now passes a groupKey
    private val onVerifyToggle: (CatatanKasItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is RiwayatKasListItem.Header -> TYPE_HEADER
            is RiwayatKasListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ItemHeaderTanggalKasBinding.inflate(inflater, parent, false))
            TYPE_ITEM -> ItemViewHolder(ItemCatatanKasBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = items[position]) {
            is RiwayatKasListItem.Header -> (holder as HeaderViewHolder).bind(currentItem)
            is RiwayatKasListItem.Item -> (holder as ItemViewHolder).bind(currentItem)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<RiwayatKasListItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    fun toggleSelection(catatanId: Int) {
        if (selectedItems.contains(catatanId)) {
            selectedItems.remove(catatanId)
        } else {
            selectedItems.add(catatanId)
        }
        notifyDataSetChanged()
    }

    private fun getGroupKey(item: CatatanKasItem): String {
        return "${item.tanggalCatat}|${item.namaSetoran}"
    }

    private fun getGroupKey(group: TanggalGroup): String {
        return "${group.tanggalCatat}|${group.namaSetoran ?: ""}"
    }
    
    fun toggleSelectAllInGroup(groupKey: String) {
        val groupItemIds = items.filterIsInstance<RiwayatKasListItem.Item>()
                                .filter { getGroupKey(it.catatan) == groupKey }
                                .map { it.catatan.id }
        
        val allSelected = groupItemIds.isNotEmpty() && groupItemIds.all { selectedItems.contains(it) }
        
        if (allSelected) {
            selectedItems.removeAll(groupItemIds)
        } else {
            selectedItems.addAll(groupItemIds)
        }
        notifyDataSetChanged()
    }

    fun getSelectedItemsInGroup(groupKey: String): List<CatatanKasItem> {
        val groupItems = items.filterIsInstance<RiwayatKasListItem.Item>()
                              .filter { getGroupKey(it.catatan) == groupKey }
                              .map { it.catatan }
        return groupItems.filter { selectedItems.contains(it.id) }
    }
    
    fun clearSelection(){
        selectedItems.clear()
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(private val binding: ItemHeaderTanggalKasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatKasListItem.Header) {
            val group = item.group
            val verifiedCount = group.list.count { it.verified == 1 }
            val unverifiedCount = group.list.size - verifiedCount

            binding.tvHeaderTanggal.text = group.tanggalCatat
            binding.tvNamaSetoran.text = group.namaSetoran ?: "-"
            
            val totalNominalFormatted = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(group.totalNominal)
            binding.tvHeaderSummary.text = "V:${verifiedCount}, U:${unverifiedCount} | $totalNominalFormatted"
            
            // Show technician name for the setoran
            binding.tvRekapHarian.text = "Teknisi: ${group.namaTeknisi ?: "-"}"
            binding.tvRekapHarian.visibility = View.VISIBLE

            val groupKey = getGroupKey(group)
            binding.btnSelectAll.setOnClickListener { onSelectAllClick(groupKey) }
            binding.btnPrintTanggal.setOnClickListener { onPrintClick(groupKey) }
        }
    }

    inner class ItemViewHolder(private val binding: ItemCatatanKasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatKasListItem.Item) {
            val isSelected = selectedItems.contains(item.catatan.id)
            binding.root.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.TRANSPARENT)
            
            binding.tvNamaPelangganCatatan.text = item.catatan.namaPelanggan
            binding.tvIdPelangganCatatan.text = "ID: ${item.catatan.idPelanggan ?: "-"}"
            binding.tvWilayahCatatan.text = "Wilayah: ${item.catatan.wilayah ?: "-"}"
            binding.tvMikrotikUsernameCatatan.text = "Username: ${item.catatan.mikrotikUsername ?: "-"}"
            binding.tvNamaTeknisiCatatan.text = "Teknisi: ${item.catatan.namaTeknisi ?: "N/A"}"
            binding.btnDeleteCatatan.setOnClickListener { onDeleteClick(item.catatan) }
            binding.root.setOnClickListener { onItemClick(item.catatan) }

            binding.switchVerified.isChecked = item.catatan.verified == 1
            binding.switchVerified.setOnClickListener { onVerifyToggle(item.catatan) }

            // Show/Hide delete button based on verification status
            if (item.catatan.verified == 1) {
                binding.btnDeleteCatatan.visibility = View.GONE
            } else {
                binding.btnDeleteCatatan.visibility = View.VISIBLE
            }
        }
    }
}
