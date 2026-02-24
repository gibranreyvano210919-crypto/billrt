package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganOfflineBinding
import com.linkbit.billrt.model.PppoeOfflineUser

class PelangganOfflineAdapter(
    private var userList: List<PppoeOfflineUser>
) : RecyclerView.Adapter<PelangganOfflineAdapter.ViewHolder>() {

    private var filteredList: List<PppoeOfflineUser> = userList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganOfflineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount() = filteredList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<PppoeOfflineUser>) {
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

    inner class ViewHolder(private val binding: ItemPelangganOfflineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: PppoeOfflineUser) {
            binding.tvName.text = user.name
            binding.tvProfile.text = user.profile
            // Tambahkan logika lain jika perlu, misal untuk status disabled
        }
    }
}
