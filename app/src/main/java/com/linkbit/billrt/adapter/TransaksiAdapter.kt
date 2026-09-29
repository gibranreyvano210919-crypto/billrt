package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemTransaksiBinding
import com.linkbit.billrt.model.TransaksiItem
import java.text.NumberFormat
import java.util.Locale

class TransaksiAdapter(private var transaksiList: List<TransaksiItem>) : RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val binding = ItemTransaksiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransaksiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        holder.bind(transaksiList[position])
    }

    override fun getItemCount() = transaksiList.size

    fun updateData(newTransaksiList: List<TransaksiItem>) {
        transaksiList = newTransaksiList
        notifyDataSetChanged()
    }

    inner class TransaksiViewHolder(private val binding: ItemTransaksiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaksi: TransaksiItem) {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            numberFormat.minimumFractionDigits = 0

            binding.apply {
                tvNamaPelangganTransaksi.text = transaksi.namaPelanggan
                tvUsernameTransaksi.text = transaksi.username
                // Menggunakan jam_bayar karena fokus pada transaksi harian
                tvTanggalTransaksi.text = transaksi.jamBayar
                tvInvoiceTransaksi.text = "ID: ${transaksi.idPembayaran}"
                tvPeriodeTransaksi.text = "Periode: ${transaksi.periode}"
                tvJumlahTransaksi.text = numberFormat.format(transaksi.jumlah)
                tvMetodeTransaksi.text = "Metode: ${transaksi.metode}"
                tvAdminTransaksi.text = "Admin: ${transaksi.admin}"
                tvKeteranganTransaksi.text = "Ket: ${transaksi.keterangan}"
            }
        }
    }
}
