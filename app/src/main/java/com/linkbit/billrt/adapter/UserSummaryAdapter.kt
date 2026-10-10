package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.databinding.ItemUserSummaryChipBinding
import com.linkbit.billrt.model.PerUserPencatat

class UserSummaryAdapter(
    private val list: List<PerUserPencatat>,
    private val onUserClick: (Int?) -> Unit
) : RecyclerView.Adapter<UserSummaryAdapter.ViewHolder>() {

    private var selectedUserId: Int? = null

    inner class ViewHolder(private val binding: ItemUserSummaryChipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PerUserPencatat) {
            binding.tvUserName.text = item.namaLengkap
            binding.tvUserCount.text = item.totalTransaksi.toString()

            val isSelected = selectedUserId == item.idUserPencatat
            
            val context = binding.root.context
            if (isSelected) {
                binding.cardUser.setCardBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))
                binding.tvUserName.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                binding.tvUserCount.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_white)
                binding.tvUserCount.setTextColor(ContextCompat.getColor(context, R.color.purple_500))
            } else {
                binding.cardUser.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                binding.tvUserName.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvUserCount.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_primary)
                binding.tvUserCount.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }

            binding.root.setOnClickListener {
                selectedUserId = if (selectedUserId == item.idUserPencatat) null else item.idUserPencatat
                notifyDataSetChanged()
                onUserClick(selectedUserId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserSummaryChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    fun resetSelection() {
        selectedUserId = null
        notifyDataSetChanged()
    }
}
