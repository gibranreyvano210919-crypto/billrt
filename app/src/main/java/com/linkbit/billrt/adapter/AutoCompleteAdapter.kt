package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemAutocompleteBinding
import com.linkbit.billrt.model.AutoCompleteItem

class AutoCompleteAdapter(
    private var items: List<AutoCompleteItem>,
    private val onItemClick: (AutoCompleteItem) -> Unit
) : RecyclerView.Adapter<AutoCompleteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAutocompleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AutoCompleteItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAutocompleteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AutoCompleteItem) {
            binding.text1.text = item.namaPelanggan
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
