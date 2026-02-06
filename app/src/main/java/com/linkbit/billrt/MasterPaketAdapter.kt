package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemMasterPaketBinding

class MasterPaketAdapter(
    private var paketList: List<Paket>,
    private val onEditClick: (Paket) -> Unit,
    private val onDeleteClick: (Paket) -> Unit
) : RecyclerView.Adapter<MasterPaketAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMasterPaketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(paketList[position])
    }

    override fun getItemCount() = paketList.size

    fun updateData(newPaketList: List<Paket>) {
        paketList = newPaketList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMasterPaketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(paket: Paket) {
            binding.tvNamaPaket.text = paket.nama_paket
            binding.tvHargaPaket.text = "Rp ${paket.harga}"
            binding.btnEditPaket.setOnClickListener { onEditClick(paket) }
            binding.btnDeletePaket.setOnClickListener { onDeleteClick(paket) }
        }
    }
}