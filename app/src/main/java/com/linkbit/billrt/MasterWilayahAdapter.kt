package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMasterWilayahBinding

class MasterWilayahAdapter(
    private var wilayahList: List<Wilayah>,
    private val onEditClick: (Wilayah) -> Unit,
    private val onDeleteClick: (Wilayah) -> Unit
) : RecyclerView.Adapter<MasterWilayahAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMasterWilayahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(wilayahList[position])
    }

    override fun getItemCount() = wilayahList.size

    fun updateData(newWilayahList: List<Wilayah>) {
        wilayahList = newWilayahList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMasterWilayahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wilayah: Wilayah) {
            binding.tvNamaWilayah.text = wilayah.nama_wilayah
            binding.btnEditWilayah.setOnClickListener { onEditClick(wilayah) }
            binding.btnDeleteWilayah.setOnClickListener { onDeleteClick(wilayah) }
        }
    }
}