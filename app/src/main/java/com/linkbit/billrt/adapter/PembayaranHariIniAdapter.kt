package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPembayaranHariIniBinding
import com.linkbit.billrt.model.PembayaranHariIni
import java.text.NumberFormat
import java.util.Locale

class PembayaranHariIniAdapter(
    private val list: List<PembayaranHariIni>,
    private val userId: Int
) : RecyclerView.Adapter<PembayaranHariIniAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPembayaranHariIniBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PembayaranHariIni) {
            binding.tvNomorUrut.text = item.no.toString()
            binding.tvNamaPelanggan.text = item.namaPelanggan
            binding.tvPeriode.text = item.periode

            if (userId != 1) {
                binding.tvJumlahBayar.text = "Rp ••••••"
            } else {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                binding.tvJumlahBayar.text = formatRupiah.format(item.jumlah).replace("Rp", "Rp ")
            }

            binding.tvMetodeBayar.text = item.metode
            binding.tvWaktu.text = item.waktuSistem
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

    override fun getItemCount(): Int = list.size
}
