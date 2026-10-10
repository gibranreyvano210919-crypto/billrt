package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
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
        filteredList = newList
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
            binding.tvProfile.text = "Profile: ${user.profile}"
            
            // Tampilkan Last Logged Out jika ada
            if (!user.lastLoggedOut.isNullOrBlank()) {
                binding.tvLastLoggedOut.text = "Logout: ${user.lastLoggedOut}"
                binding.tvLastLoggedOut.visibility = View.VISIBLE
            } else {
                binding.tvLastLoggedOut.visibility = View.GONE
            }

            // Tampilkan Comment jika ada dan bukan strip
            if (!user.comment.isNullOrBlank() && user.comment != "-") {
                binding.tvComment.text = user.comment
                binding.tvComment.visibility = View.VISIBLE
            } else {
                binding.tvComment.visibility = View.GONE
            }
        }
    }
}
