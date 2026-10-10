package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganSyncBinding
import com.linkbit.billrt.model.PelangganSyncItem

class SyncMikrotikAdapter(
    private val onSyncClick: (PelangganSyncItem) -> Unit
) : ListAdapter<PelangganSyncItem, SyncMikrotikAdapter.ViewHolder>(DiffCallback()) {

    var isBatchSyncing: Boolean = false
        private set

    @SuppressLint("NotifyDataSetChanged")
    fun setBatchSyncing(isBatch: Boolean) {
        if (isBatchSyncing != isBatch) {
            isBatchSyncing = isBatch
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganSyncBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPelangganSyncBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PelangganSyncItem) {
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvIdPelanggan.text = "ID: ${item.idPelanggan}"
            
            val usernameStr = if (!item.mikrotikUsername.isNullOrEmpty()) item.mikrotikUsername else "-"
            binding.tvUsernameMikrotik.text = "User MikroTik: $usernameStr"
            binding.tvMacAddress.text = "MAC Address: ${item.macAddress ?: "-"}"
            binding.tvStaticIp.text = "IP Address: ${item.staticIp ?: "-"}"

            // Hilangkan progress bar kecil di item jika sedang batch sync
            if (item.isSyncing && !isBatchSyncing) {
                binding.pbSyncSingle.visibility = View.VISIBLE
                binding.btnSyncSingle.visibility = View.INVISIBLE
                binding.btnSyncSingle.isEnabled = false
            } else {
                binding.pbSyncSingle.visibility = View.GONE
                binding.btnSyncSingle.visibility = View.VISIBLE
                binding.btnSyncSingle.isEnabled = !isBatchSyncing
            }

            if (!item.lastSyncStatus.isNullOrEmpty()) {
                binding.tvSyncStatus.visibility = View.VISIBLE
                binding.tvSyncStatus.text = item.lastSyncStatus

                when {
                    item.hasChanged == true -> {
                        binding.tvSyncStatus.setTextColor(Color.parseColor("#1B5E20")) // Bold Green
                        binding.tvSyncStatus.setTypeface(null, Typeface.BOLD)
                    }
                    item.isSuccess == true -> {
                        binding.tvSyncStatus.setTextColor(Color.parseColor("#455A64")) // Slate Gray
                        binding.tvSyncStatus.setTypeface(null, Typeface.ITALIC)
                    }
                    item.isSuccess == false -> {
                        binding.tvSyncStatus.setTextColor(Color.parseColor("#C62828")) // Red
                        binding.tvSyncStatus.setTypeface(null, Typeface.NORMAL)
                    }
                    else -> {
                        binding.tvSyncStatus.setTextColor(Color.parseColor("#1565C0")) // Blue (Progress)
                        binding.tvSyncStatus.setTypeface(null, Typeface.ITALIC)
                    }
                }
            } else {
                binding.tvSyncStatus.visibility = View.GONE
            }

            binding.btnSyncSingle.setOnClickListener {
                if (!isBatchSyncing) {
                    onSyncClick(item)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PelangganSyncItem>() {
        override fun areItemsTheSame(
            oldItem: PelangganSyncItem,
            newItem: PelangganSyncItem
        ): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(
            oldItem: PelangganSyncItem,
            newItem: PelangganSyncItem
        ): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan &&
                    oldItem.macAddress == newItem.macAddress &&
                    oldItem.staticIp == newItem.staticIp &&
                    oldItem.isSyncing == newItem.isSyncing &&
                    oldItem.lastSyncStatus == newItem.lastSyncStatus &&
                    oldItem.isSuccess == newItem.isSuccess &&
                    oldItem.hasChanged == newItem.hasChanged
        }
    }
}
