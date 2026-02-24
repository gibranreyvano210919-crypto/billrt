package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganOnlineBinding
import com.linkbit.billrt.model.PppoeOnlineUser

class PelangganOnlineAdapter(
    private var userList: List<PppoeOnlineUser>,
    private val onKickClickListener: (PppoeOnlineUser) -> Unit
) : RecyclerView.Adapter<PelangganOnlineAdapter.ViewHolder>() {

    private var filteredList: List<PppoeOnlineUser> = userList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganOnlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount() = filteredList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<PppoeOnlineUser>) {
        userList = newList
        filteredList = newList // Tanpa filter, daftar yang ditampilkan sama dengan daftar asli
        notifyDataSetChanged()
    }

    fun filter(query: String?) {
        filteredList = if (query.isNullOrBlank()) {
            userList
        } else {
            userList.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganOnlineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: PppoeOnlineUser) {
            binding.tvName.text = user.name
            binding.tvAddress.text = user.address
            binding.tvUptime.text = user.uptime
            binding.tvCallerId.text = user.callerId
            binding.btnKick.setOnClickListener { onKickClickListener(user) }
        }
    }
}
