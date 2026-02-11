package com.linkbit.billrt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.model.HistoriCatat

class HistoriCatatAdapter(private val historiCatatList: List<HistoriCatat>) : RecyclerView.Adapter<HistoriCatatAdapter.HistoriCatatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriCatatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_histori_catat, parent, false)
        return HistoriCatatViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoriCatatViewHolder, position: Int) {
        val historiCatat = historiCatatList[position]
        holder.bind(historiCatat)
    }

    override fun getItemCount(): Int = historiCatatList.size

    inner class HistoriCatatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tv_nama_pelanggan)
        private val tvIdPelanggan: TextView = itemView.findViewById(R.id.tv_id_pelanggan)
        private val tvMikrotikUsername: TextView = itemView.findViewById(R.id.tv_mikrotik_username)
        private val tvWilayah: TextView = itemView.findViewById(R.id.tv_wilayah)
        private val rvRiwayatPeriode: RecyclerView = itemView.findViewById(R.id.rv_riwayat_periode)

        fun bind(historiCatat: HistoriCatat) {
            tvNamaPelanggan.text = historiCatat.nama_pelanggan
            tvIdPelanggan.text = historiCatat.id_pelanggan
            tvMikrotikUsername.text = historiCatat.mikrotik_username
            tvWilayah.text = historiCatat.wilayah

            rvRiwayatPeriode.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = RiwayatPeriodeAdapter(historiCatat.riwayat_per_periode)
            }
        }
    }
}
