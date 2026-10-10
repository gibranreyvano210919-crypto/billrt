package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPencarianCepatBinding

class PencarianCepatAdapter(
    private var items: List<PelangganData>,
    private val onCopyMacClick: (String) -> Unit,
    private val onItemLongClick: (PelangganData) -> Unit
) : RecyclerView.Adapter<PencarianCepatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPencarianCepatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<PelangganData>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPencarianCepatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PelangganData) {
            binding.tvNamaPelanggan.text = item.nama
            binding.tvAlamat.text = item.alamat
            binding.tvWilayah.text = item.namaWilayah ?: "N/A"
            binding.tvMikrotikUsername.text = item.mikrotikUsername ?: "N/A"
            binding.tvStaticIp.text = item.staticIp ?: "N/A"
            binding.tvMacAddress.text = item.macAddress ?: "-"

            val pmb = item.pembayaranTerakhir
            if (pmb != null && (!pmb.periode.isNullOrEmpty() || !pmb.tanggalBayar.isNullOrEmpty())) {
                binding.dividerPembayaran.visibility = View.VISIBLE
                binding.layoutPembayaranTerakhir.visibility = View.VISIBLE

                // Periode
                binding.tvPeriodeBayarTerakhir.text = "Bayar Terakhir: ${pmb.periode ?: "-"}"

                // Tanggal
                val tgl = pmb.tanggalBayar ?: "-"
                binding.tvDetailBayarTerakhir.text = "Tanggal: $tgl"

                // Pencatat
                binding.tvPencatatBayarTerakhir.text = "Pencatat: ${pmb.namaPencatat ?: "-"}"
            } else {
                binding.dividerPembayaran.visibility = View.GONE
                binding.layoutPembayaranTerakhir.visibility = View.GONE
            }

            if (!item.macAddress.isNullOrEmpty()) {
                binding.btnCopyMac.setOnClickListener { 
                    onCopyMacClick(item.macAddress)
                }
            } else {
                binding.btnCopyMac.setOnClickListener(null)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}
