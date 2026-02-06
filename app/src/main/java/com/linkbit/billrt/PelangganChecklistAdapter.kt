package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPelangganCheckableBinding
import java.util.Locale

class PelangganChecklistAdapter(
    private var allPelanggan: List<PelangganData>,
    private val onPelangganSelected: (List<PelangganData>) -> Unit
) : RecyclerView.Adapter<PelangganChecklistAdapter.ViewHolder>() {

    private var filteredPelanggan = ArrayList(allPelanggan)
    private var selectedPelangganIds = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganCheckableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredPelanggan[position])
    }

    override fun getItemCount() = filteredPelanggan.size

    fun getSelectedPelanggans(): List<PelangganData> {
        return allPelanggan.filter { selectedPelangganIds.contains(it.idPelanggan) }
    }

    fun filter(query: String?) {
        filteredPelanggan.clear()
        if (query.isNullOrEmpty()) {
            filteredPelanggan.addAll(allPelanggan)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            for (pelanggan in allPelanggan) {
                if (pelanggan.nama.lowercase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredPelanggan.add(pelanggan)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun updateData(newPelanggan: List<PelangganData>) {
        allPelanggan = newPelanggan
        filter(null)
    }

    fun clearSelection() {
        selectedPelangganIds.clear()
        onPelangganSelected(emptyList())
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPelangganCheckableBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pelanggan = filteredPelanggan[position]
                    if (selectedPelangganIds.contains(pelanggan.idPelanggan)) {
                        selectedPelangganIds.remove(pelanggan.idPelanggan)
                    } else {
                        selectedPelangganIds.add(pelanggan.idPelanggan)
                    }
                    notifyItemChanged(position)
                    onPelangganSelected(getSelectedPelanggans())
                }
            }
        }

        fun bind(pelanggan: PelangganData) {
            binding.tvNamaPelangganCheckable.text = pelanggan.nama
            binding.tvWilayahCheckable.text = "Wilayah: ${pelanggan.namaWilayah ?: "-"}"
            binding.tvMikrotikUsernameCheckable.text = "Username: ${pelanggan.mikrotikUsername ?: "-"}"
            binding.checkboxPelanggan.isChecked = selectedPelangganIds.contains(pelanggan.idPelanggan)
        }
    }
}