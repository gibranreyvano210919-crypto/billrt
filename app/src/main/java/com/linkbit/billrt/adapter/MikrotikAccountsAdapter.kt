package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMikrotikAccountBinding
import com.linkbit.billrt.model.MikrotikAccount

class MikrotikAccountsAdapter(
    private var accountList: List<MikrotikAccount>,
    private val onClick: (MikrotikAccount) -> Unit,
    private val onLongClick: (MikrotikAccount) -> Unit
) : RecyclerView.Adapter<MikrotikAccountsAdapter.ViewHolder>() {

    private var defaultRouterId: Int = 0

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

    @SuppressLint("NotifyDataSetChanged")
    fun updateDefaultRouter(id: Int) {
        defaultRouterId = id
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMikrotikAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: MikrotikAccount) {
            binding.tvRouterName.text = account.routerName
            binding.tvIpAddress.text = account.ipAddress
            
            // Tampilkan centang jika router ini adalah default
            binding.ivDefaultCheck.visibility = if (account.id == defaultRouterId) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.root.setOnClickListener { onClick(account) }
            binding.root.setOnLongClickListener {
                onLongClick(account)
                true
            }
        }
    }
}
