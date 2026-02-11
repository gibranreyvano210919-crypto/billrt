package com.linkbit.billrt.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.linkbit.billrt.R
import com.linkbit.billrt.model.RiwayatPeriode

class RiwayatPeriodeAdapter(private val riwayatPeriodeList: List<RiwayatPeriode>) : RecyclerView.Adapter<RiwayatPeriodeAdapter.RiwayatPeriodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatPeriodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_periode, parent, false)
        return RiwayatPeriodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatPeriodeViewHolder, position: Int) {
        val riwayatPeriode = riwayatPeriodeList[position]
        holder.bind(riwayatPeriode)
    }

    override fun getItemCount(): Int = riwayatPeriodeList.size

    inner class RiwayatPeriodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPeriode: TextView = itemView.findViewById(R.id.tv_periode)
        private val tvDetailCatat: TextView = itemView.findViewById(R.id.tv_detail_catat)

        fun bind(riwayatPeriode: RiwayatPeriode) {
            tvPeriode.text = riwayatPeriode.periode
            val detailCatatText = riwayatPeriode.detail_catat.joinToString("\n") { 
                "Tanggal: ${it.tanggal}, Teknisi: ${it.nama_teknisi ?: "N/A"}"
            }
            tvDetailCatat.text = detailCatatText

            if (riwayatPeriode.detail_catat.any { it.is_telat == 1 }) {
                tvDetailCatat.setTextColor(Color.RED)
            } else {
                tvDetailCatat.setTextColor(Color.BLACK) // Or your default color
            }
        }
    }
}
