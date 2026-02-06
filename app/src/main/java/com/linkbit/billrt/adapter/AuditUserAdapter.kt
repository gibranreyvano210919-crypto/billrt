package com.linkbit.billrt.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemAuditUserBinding
import com.linkbit.billrt.AuditResult

class AuditUserAdapter(
    private var auditResults: List<AuditResult>
) : RecyclerView.Adapter<AuditUserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAuditUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(auditResults[position])
    }

    override fun getItemCount() = auditResults.size

    fun updateData(newAuditResults: List<AuditResult>) {
        auditResults = newAuditResults
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAuditUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(auditResult: AuditResult) {
            binding.tvUsername.text = auditResult.username
            binding.tvInfo.text = auditResult.info
            binding.tvStatus.text = auditResult.status

            when (auditResult.status) {
                "mismatch" -> binding.tvStatus.setTextColor(Color.BLUE)
                "missing" -> binding.tvStatus.setTextColor(Color.RED)
                else -> binding.tvStatus.setTextColor(Color.BLACK)
            }
        }
    }
}
