package com.linkbit.billrt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.databinding.ItemPembayaranBinding
import java.text.NumberFormat
import java.util.Locale

class BayarAdapter(private val pembayaranList: List<PembayaranData>) : 
    RecyclerView.Adapter<BayarAdapter.BayarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BayarViewHolder {
        val binding = ItemPembayaranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BayarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BayarViewHolder, position: Int) {
        holder.bind(pembayaranList[position])
    }

    override fun getItemCount() = pembayaranList.size

    class BayarViewHolder(private val binding: ItemPembayaranBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pembayaran: PembayaranData) {
            binding.tvNamaPelangganBayar.text = pembayaran.namaPelanggan
            binding.tvAlamatPelangganBayar.text = pembayaran.alamatPelanggan
            binding.tvTanggalBayar.text = "Dibayar pada: ${pembayaran.tglBayar}"

            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvJumlahBayar.text = format.format(pembayaran.jumlahBayar)
        }
    }
}
