package com.linkbit.billrt.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemSemuaPelangganBinding
import com.linkbit.billrt.model.SemuaPelanggan

class SemuaPelangganAdapter(
    private var pelangganList: List<SemuaPelanggan>,
    private val onEditClick: (SemuaPelanggan) -> Unit
) : RecyclerView.Adapter<SemuaPelangganAdapter.ViewHolder>() {

    private var filteredList: List<SemuaPelanggan> = pelangganList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSemuaPelangganBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            pelangganList
        } else {
            pelangganList.filter {
                it.nama?.contains(query, true) == true ||
                it.alamat?.contains(query, true) == true ||
                it.idPelanggan?.contains(query, true) == true
            }
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<SemuaPelanggan>){
        pelangganList = newList
        filter("")
    }

    inner class ViewHolder(private val binding: ItemSemuaPelangganBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: SemuaPelanggan) {
            binding.namaTextView.text = pelanggan.nama
            binding.alamatTextView.text = pelanggan.alamat
            binding.wilayahTextView.text = ""
            binding.koordinatTextView.text = "Lihat di Peta"

            binding.koordinatTextView.setOnClickListener {
                // Implement maps functionality here
            }

            binding.browserButton.visibility = View.GONE

            binding.editLokasiButton.setOnClickListener { onEditClick(pelanggan) }
        }
    }
}
