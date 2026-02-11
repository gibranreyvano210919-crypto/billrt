package com.linkbit.billrt.model

data class HistoriCatat(
    val id_pelanggan: String,
    val nama_pelanggan: String,
    val mikrotik_username: String,
    val wilayah: String,
    val riwayat_per_periode: List<RiwayatPeriode>
)
