package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganTanpaLokasiBinding
import com.linkbit.billrt.model.PelangganTanpaLokasi

class PelangganTanpaLokasiAdapter(
    private var pelangganList: List<PelangganTanpaLokasi>,
    private val onUpdateClick: (PelangganTanpaLokasi) -> Unit
) : RecyclerView.Adapter<PelangganTanpaLokasiAdapter.ViewHolder>() {

    private var filteredList: List<PelangganTanpaLokasi> = pelangganList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganTanpaLokasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                it.nama.contains(query, true) == true ||
                it.alamat.contains(query, true) == true ||
                it.idPelanggan.contains(query, true) == true
            }
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<PelangganTanpaLokasi>){
        pelangganList = newList
        filter("")
    }

    inner class ViewHolder(private val binding: ItemPelangganTanpaLokasiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelanggan: PelangganTanpaLokasi) {
            binding.namaTextView.text = pelanggan.nama
            binding.alamatTextView.text = pelanggan.alamat
            binding.wilayahTextView.text = ""
            binding.updateButton.setOnClickListener { onUpdateClick(pelanggan) }
        }
    }
}
