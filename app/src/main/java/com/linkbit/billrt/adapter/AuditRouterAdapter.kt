package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemAuditRouterBinding
import com.linkbit.billrt.viewmodel.AuditRouterItem

class AuditRouterAdapter(private var items: List<AuditRouterItem>) :
    RecyclerView.Adapter<AuditRouterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAuditRouterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AuditRouterItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAuditRouterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AuditRouterItem) {
            binding.tvRouterName.text = item.router
            
            // Render Hilang di Router
            if (item.hilangDiRouter.total > 0) {
                binding.tvHilangList.text = item.hilangDiRouter.list.joinToString("\n") { "• $it" }
                binding.tvHilangHeader.text = "Hilang di Router (${item.hilangDiRouter.total}):"
            } else {
                binding.tvHilangList.text = "Semua user DB sinkron di Router"
                binding.tvHilangHeader.text = "Hilang di Router (0):"
            }

            // Render User Gelap
            if (item.userGelap.total > 0) {
                binding.tvGelapList.text = item.userGelap.list.joinToString("\n") { "• $it" }
                binding.tvGelapHeader.text = "User Gelap di Mikrotik (${item.userGelap.total}):"
            } else {
                binding.tvGelapList.text = "Tidak ada user gelap"
                binding.tvGelapHeader.text = "User Gelap di Mikrotik (0):"
            }
        }
    }
}
