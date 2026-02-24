package com.linkbit.billrt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemDaftarSecretBinding
import com.linkbit.billrt.model.PppoeSecret

class DaftarSecretAdapter(
    private var secretList: List<PppoeSecret>,
    private val onStatusChange: (PppoeSecret, Boolean) -> Unit
) : RecyclerView.Adapter<DaftarSecretAdapter.ViewHolder>() {

    private var filteredList: List<PppoeSecret> = secretList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDaftarSecretBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount() = filteredList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<PppoeSecret>) {
        secretList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String?) {
        filteredList = if (query.isNullOrBlank()) {
            secretList
        } else {
            secretList.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemDaftarSecretBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(secret: PppoeSecret) {
            binding.tvName.text = secret.name
            binding.tvProfile.text = "Profile: ${secret.profile}"
            binding.tvComment.text = "Comment: ${secret.comment}"

            binding.switchStatus.setOnCheckedChangeListener(null) // Hapus listener lama
            binding.switchStatus.isChecked = secret.disabled == "false"
            binding.switchStatus.text = if (binding.switchStatus.isChecked) "Enabled" else "Disabled"

            binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
                onStatusChange(secret, isChecked)
            }
        }
    }
}
