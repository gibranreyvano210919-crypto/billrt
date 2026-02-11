package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemWilayahFilterBinding

class WilayahFilterAdapter(
    private var wilayahList: List<Pair<String, Int>>,
    private val onWilayahClick: (String) -> Unit
) : RecyclerView.Adapter<WilayahFilterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWilayahFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(wilayahList[position])
    }

    override fun getItemCount() = wilayahList.size

    inner class ViewHolder(private val binding: ItemWilayahFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wilayah: Pair<String, Int>) {
            binding.tvWilayahName.text = "${wilayah.first} (${wilayah.second})"
            binding.root.setOnClickListener {
                onWilayahClick(wilayah.first)
            }
        }
    }
}