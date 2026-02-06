package com.linkbit.billrt.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.linkbit.billrt.databinding.ItemPelangganPusatBinding
import com.linkbit.billrt.databinding.ItemPelangganTetanggaBinding
import com.linkbit.billrt.model.PelangganPusat
import com.linkbit.billrt.model.PelangganTetangga

class RadiusPelangganAdapter(private var items: List<Any>) : RecyclerView.Adapter<RadiusPelangganAdapter.BaseViewHolder>() {

    companion object {
        private const val TYPE_PUSAT = 0
        private const val TYPE_TETANGGA = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_PUSAT -> {
                val binding = ItemPelangganPusatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PusatViewHolder(binding)
            }
            TYPE_TETANGGA -> {
                val binding = ItemPelangganTetanggaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TetanggaViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PelangganPusat -> TYPE_PUSAT
            is PelangganTetangga -> TYPE_TETANGGA
            else -> throw IllegalArgumentException("Invalid type of data at position $position")
        }
    }

    fun updateData(pusat: PelangganPusat?, tetangga: List<PelangganTetangga>?) {
        val newItems = mutableListOf<Any>()
        pusat?.let { newItems.add(it) }
        tetangga?.let { newItems.addAll(it) }
        items = newItems
        notifyDataSetChanged()
    }

    private fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label disalin: $text", Toast.LENGTH_SHORT).show()
    }

    abstract class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: Any)
    }

    inner class PusatViewHolder(private val binding: ItemPelangganPusatBinding) : BaseViewHolder(binding) {
        override fun bind(item: Any) {
            val pelanggan = item as PelangganPusat
            binding.namaTextView.text = pelanggan.nama ?: "N/A"
            binding.statusTextView.text = "Status: ${pelanggan.statusAktif ?: "N/A"}"
            val macAddress = pelanggan.macAddress ?: "-"
            binding.macTextView.text = "MAC: $macAddress"

            binding.copyButton.setOnClickListener {
                if (macAddress != "-") {
                    copyToClipboard(it.context, "MAC Address", macAddress)
                }
            }
        }
    }

    inner class TetanggaViewHolder(private val binding: ItemPelangganTetanggaBinding) : BaseViewHolder(binding) {
        override fun bind(item: Any) {
            val pelanggan = item as PelangganTetangga
            binding.namaTextView.text = pelanggan.nama ?: "N/A"
            binding.jarakTextView.text = "Jarak: ${pelanggan.jarak ?: "N/A"}"
            val macAddress = pelanggan.macAddress ?: "-"
            binding.macTextView.text = "MAC: $macAddress"

            binding.copyButton.setOnClickListener {
                if (macAddress != "-") {
                    copyToClipboard(it.context, "MAC Address", macAddress)
                }
            }
        }
    }
}
