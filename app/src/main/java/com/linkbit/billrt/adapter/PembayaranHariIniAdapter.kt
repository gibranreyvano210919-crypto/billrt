package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPembayaranHariIniBinding
import com.linkbit.billrt.model.PembayaranHariIni
import java.text.NumberFormat
import java.util.Locale

class PembayaranHariIniAdapter(private val list: List<PembayaranHariIni>) :
    RecyclerView.Adapter<PembayaranHariIniAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPembayaranHariIniBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PembayaranHariIni) {
            binding.tvNomorUrut.text = item.no.toString()
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvPeriode.text = item.periode

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvJumlahBayar.text = formatRupiah.format(item.jumlah.toDoubleOrNull() ?: 0.0)

            binding.tvMetodeBayar.text = item.metode
            binding.tvWaktu.text = item.waktu
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPembayaranHariIniBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
