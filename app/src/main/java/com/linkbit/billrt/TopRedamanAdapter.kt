package com.linkbit.billrt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemTopRedamanBinding

class TopRedamanAdapter(private val items: List<TopRedamanItem>) : RecyclerView.Adapter<TopRedamanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopRedamanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemTopRedamanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopRedamanItem) {
            binding.tvOltName.text = item.olt
            binding.tvOnuIndex.text = item.onu
            binding.tvMacAddress.text = item.mac
            binding.tvSignal.text = item.signal.toString()
            binding.tvStatus.text = item.status

            binding.btnCopyMac.setImageResource(android.R.drawable.ic_menu_save)

            binding.btnCopyMac.setOnClickListener {
                val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("MAC Address", item.mac)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(itemView.context, "MAC Address disalin", Toast.LENGTH_SHORT).show()
            }
        }
    }
}