package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemMikrotikBillingBinding
import com.linkbit.billrt.model.MikrotikBillingItem

class MikrotikBillingAdapter(
    private var billingList: List<MikrotikBillingItem>,
    private val onItemClick: (MikrotikBillingItem) -> Unit,
    private val onItemLongClick: (MikrotikBillingItem) -> Unit
) : RecyclerView.Adapter<MikrotikBillingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMikrotikBillingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(billingList[position])
    }

    override fun getItemCount() = billingList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<MikrotikBillingItem>) {
        billingList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMikrotikBillingBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: MikrotikBillingItem) {
            binding.tvNama.text = "${item.noUrut}. ${item.namaPelanggan}"
            binding.tvUsername.text = "@${item.mikrotikUsername}"
            binding.tvPhone.text = item.teleponPelanggan
            binding.tvExpired.text = "Exp: ${item.tglExpired}"
            binding.tvRouter.text = item.routerName
            
            // Tampilkan MAC Address jika ada
            if (item.macAddress.isNotEmpty()) {
                binding.tvUsername.append(" | ${item.macAddress}")
            }
            binding.tvStatusBilling.text = "Status: ${item.statusAktif.uppercase()}"

            // Status Online
            if (item.isOnline) {
                binding.tvStatusOnline.text = "ONLINE"
                binding.tvStatusOnline.setTextColor(Color.WHITE)
                binding.tvStatusOnline.setBackgroundResource(R.drawable.bg_status_online)
            } else {
                binding.tvStatusOnline.text = "OFFLINE"
                binding.tvStatusOnline.setTextColor(Color.DKGRAY)
                binding.tvStatusOnline.setBackgroundResource(R.drawable.bg_badge_gray)
            }

            // Jika disabled di mikrotik
            if (item.isDisabled) {
                binding.tvNama.setTextColor(Color.RED)
                binding.tvStatusBilling.text = "Status: DISABLED"
                binding.tvStatusBilling.setTextColor(Color.RED)
            } else {
                binding.tvNama.setTextColor(Color.BLACK)
                binding.tvStatusBilling.setTextColor(Color.DKGRAY)
            }

            binding.root.setOnClickListener { onItemClick(item) }
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}
