package com.linkbit.billrt.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.linkbit.billrt.databinding.ItemSetoranBinding
import com.linkbit.billrt.databinding.ItemSetoranHeaderBinding
import com.linkbit.billrt.model.SetoranItem
import java.text.NumberFormat
import java.util.Locale

sealed class TimelineItem {
    data class Setoran(val setoranItem: SetoranItem) : TimelineItem()
    data class Header(val date: String) : TimelineItem()
}

class SetoranAdapter(
    private var timelineItems: List<TimelineItem>,
    private val listener: OnAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnAdapterListener {
        fun onEdit(setoran: SetoranItem)
        fun onDelete(setoran: SetoranItem)
        fun onPrint(setoran: SetoranItem)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SETORAN = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (timelineItems[position]) {
            is TimelineItem.Header -> TYPE_HEADER
            is TimelineItem.Setoran -> TYPE_SETORAN
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemSetoranHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemSetoranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SetoranViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = timelineItems[position]) {
            is TimelineItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TimelineItem.Setoran -> (holder as SetoranViewHolder).bind(item.setoranItem)
        }
    }

    override fun getItemCount(): Int = timelineItems.size

    fun updateData(newTimelineItems: List<TimelineItem>) {
        timelineItems = newTimelineItems
        notifyDataSetChanged()
    }

    inner class SetoranViewHolder(private val binding: ItemSetoranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(setoran: SetoranItem) {
            binding.tvNamaTeknisi.text = setoran.namaTeknisi ?: setoran.idTeknisi

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvNominalSetoran.text = formatRupiah.format(setoran.nominalSetor)

            binding.tvTanggalSetoran.text = setoran.tglSetoran
            binding.tvCatatan.text = setoran.catatan.replace("\\n", "\n")

            Glide.with(itemView.context)
                .load(setoran.urlCloudinary)
                .into(binding.ivBuktiSetoran)

            if (!setoran.urlCloudinary.isNullOrEmpty()) {
                binding.tvLinkFoto.visibility = View.VISIBLE
                binding.tvLinkFoto.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(setoran.urlCloudinary))
                    itemView.context.startActivity(intent)
                }
            } else {
                binding.tvLinkFoto.visibility = View.GONE
            }

            binding.btnEdit.setOnClickListener { listener.onEdit(setoran) }
            binding.btnHapus.setOnClickListener { listener.onDelete(setoran) }
            binding.btnPrint.setOnClickListener { listener.onPrint(setoran) }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemSetoranHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: TimelineItem.Header) {
            binding.tvHeaderDate.text = header.date
        }
    }
}
