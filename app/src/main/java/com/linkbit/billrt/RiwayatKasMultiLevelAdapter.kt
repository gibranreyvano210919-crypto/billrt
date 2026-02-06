package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemHeaderMingguBinding
import com.linkbit.billrt.databinding.ItemHeaderTanggalBinding
import com.linkbit.billrt.databinding.ItemHeaderTeknisiBinding
import com.linkbit.billrt.databinding.ItemRiwayatPelangganBinding

class RiwayatKasMultiLevelAdapter(
    private var items: List<RiwayatKasListItem> = emptyList(),
    // private val onDeleteClick: (PelangganJadwalItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEKNISI = 0
        private const val TYPE_MINGGU = 1
        private const val TYPE_TANGGAL = 2
        private const val TYPE_PELANGGAN = 3
    }

    sealed class RiwayatKasListItem {
        data class TeknisiHeader(val namaTeknisi: String) : RiwayatKasListItem()
        data class MingguHeader(val namaMinggu: String) : RiwayatKasListItem()
        data class TanggalHeader(val tanggal: String) : RiwayatKasListItem()
        // data class PelangganItem(val pelanggan: PelangganJadwalItem) : RiwayatKasListItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is RiwayatKasListItem.TeknisiHeader -> TYPE_TEKNISI
            is RiwayatKasListItem.MingguHeader -> TYPE_MINGGU
            is RiwayatKasListItem.TanggalHeader -> TYPE_TANGGAL
            // is RiwayatKasListItem.PelangganItem -> TYPE_PELANGGAN
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEKNISI -> TeknisiViewHolder(ItemHeaderTeknisiBinding.inflate(inflater, parent, false))
            TYPE_MINGGU -> MingguViewHolder(ItemHeaderMingguBinding.inflate(inflater, parent, false))
            TYPE_TANGGAL -> TanggalViewHolder(ItemHeaderTanggalBinding.inflate(inflater, parent, false))
            // TYPE_PELANGGAN -> PelangganViewHolder(ItemRiwayatPelangganBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is RiwayatKasListItem.TeknisiHeader -> (holder as TeknisiViewHolder).bind(item)
            is RiwayatKasListItem.MingguHeader -> (holder as MingguViewHolder).bind(item)
            is RiwayatKasListItem.TanggalHeader -> (holder as TanggalViewHolder).bind(item)
            // is RiwayatKasListItem.PelangganItem -> (holder as PelangganViewHolder).bind(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<RiwayatKasListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class TeknisiViewHolder(private val binding: ItemHeaderTeknisiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatKasListItem.TeknisiHeader) {
            binding.tvHeaderTeknisi.text = item.namaTeknisi
        }
    }

    inner class MingguViewHolder(private val binding: ItemHeaderMingguBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatKasListItem.MingguHeader) {
            binding.tvHeaderMinggu.text = item.namaMinggu
        }
    }

    inner class TanggalViewHolder(private val binding: ItemHeaderTanggalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RiwayatKasListItem.TanggalHeader) {
            binding.tvHeaderTanggal.text = item.tanggal
        }
    }

    inner class PelangganViewHolder(private val binding: ItemRiwayatPelangganBinding) : RecyclerView.ViewHolder(binding.root) {
        // fun bind(item: RiwayatKasListItem.PelangganItem) {
        //     binding.tvItemNamaPelanggan.text = item.pelanggan.namaPelanggan
        //     binding.btnDeletePelanggan.setOnClickListener { onDeleteClick(item.pelanggan) }
        // }
    }
}