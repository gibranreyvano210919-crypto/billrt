package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMikrotikAccountBinding
import com.linkbit.billrt.model.MikrotikAccount

class MikrotikAccountsAdapter(
    private var accountList: List<MikrotikAccount>,
    private val onClick: (MikrotikAccount) -> Unit
) : RecyclerView.Adapter<MikrotikAccountsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMikrotikAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accountList[position])
    }

    override fun getItemCount() = accountList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<MikrotikAccount>) {
        accountList = newList
        notifyDataSetChanged() // Cukup untuk daftar sederhana tanpa filter
    }

    inner class ViewHolder(private val binding: ItemMikrotikAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: MikrotikAccount) {
            binding.tvRouterName.text = account.routerName
            binding.tvIpAddress.text = account.ipAddress
            binding.root.setOnClickListener { onClick(account) }
        }
    }
}
