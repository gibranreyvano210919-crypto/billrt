package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPeriodeTagihanBinding
import com.linkbit.billrt.model.PeriodeTagihan

class PeriodeTagihanAdapter(
    private val onClick: (PeriodeTagihan) -> Unit
) : ListAdapter<PeriodeTagihan, PeriodeTagihanAdapter.PeriodeTagihanViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodeTagihanViewHolder {
        val binding = ItemPeriodeTagihanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeriodeTagihanViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: PeriodeTagihanViewHolder, position: Int) {
        val periode = getItem(position)
        holder.bind(periode)
    }

    class PeriodeTagihanViewHolder(
        private val binding: ItemPeriodeTagihanBinding,
        private val onClick: (PeriodeTagihan) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(periode: PeriodeTagihan) {
            binding.tvBulanTahun.text = "${periode.bulanTagihan}/${periode.tahunTagihan}"
            itemView.setOnClickListener {
                onClick(periode)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PeriodeTagihan>() {
        override fun areItemsTheSame(oldItem: PeriodeTagihan, newItem: PeriodeTagihan): Boolean {
            return oldItem.bulanTagihan == newItem.bulanTagihan && oldItem.tahunTagihan == newItem.tahunTagihan
        }

        override fun areContentsTheSame(oldItem: PeriodeTagihan, newItem: PeriodeTagihan): Boolean {
            return oldItem == newItem
        }
    }
}
