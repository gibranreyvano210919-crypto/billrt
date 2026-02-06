package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMikrotikAccountBinding
import com.linkbit.billrt.MikrotikAccount // DIUBAH: Import dari root package
import java.util.Locale

class MikrotikAccountsAdapter(
    private var originalList: List<MikrotikAccount>,
    private val onClick: (MikrotikAccount) -> Unit
) : RecyclerView.Adapter<MikrotikAccountsAdapter.ViewHolder>() {

    private var filteredList: MutableList<MikrotikAccount> = originalList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMikrotikAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount() = filteredList.size

    fun updateData(newList: List<MikrotikAccount>) {
        originalList = newList
        filter(null) // Terapkan filter yang ada atau reset
    }

    fun filter(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(originalList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            originalList.forEach {
                if (it.routerName.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    it.ipAddress.contains(lowerCaseQuery)) {
                    filteredList.add(it)
                }
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMikrotikAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: MikrotikAccount) {
            binding.tvRouterName.text = account.routerName
            binding.tvIpAddress.text = account.ipAddress
            binding.root.setOnClickListener { onClick(account) }
        }
    }
}
