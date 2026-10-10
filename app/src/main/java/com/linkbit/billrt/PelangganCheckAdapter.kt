package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCheckBinding

class PelangganCheckAdapter(
    private var pelanggan: List<PelangganData>,
    private val onCheckChanged: () -> Unit // Callback to notify fragment
) :
    RecyclerView.Adapter<PelangganCheckAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pelanggan[position])
    }

    override fun getItemCount() = pelanggan.size

    fun getSelectedItems(): List<String> = selectedItems.toList()

    fun updateList(newList: List<PelangganData>) {
        pelanggan = newList
        notifyDataSetChanged()
    }
    
    fun getSelected(): Set<String> = selectedItems

    inner class ViewHolder(private val binding: ItemPelangganCheckBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganData) {
            binding.namaPelanggan.text = pelanggan.nama
            binding.checkbox.visibility = View.VISIBLE
            
            binding.checkbox.setOnCheckedChangeListener(null) // Remove listener to prevent loops
            binding.checkbox.isChecked = selectedItems.contains(pelanggan.idPelanggan)
            binding.checkbox.isEnabled = true

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(pelanggan.idPelanggan)
                } else {
                    selectedItems.remove(pelanggan.idPelanggan)
                }
                onCheckChanged() // Notify the fragment
            }
        }
    }
}
