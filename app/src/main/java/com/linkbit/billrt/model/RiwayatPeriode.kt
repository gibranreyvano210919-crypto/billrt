package com.linkbit.billrt.model

data class RiwayatPeriode(
    val periode: String,
    val bulan_angka: Int,
    val tahun: Int,
    val jumlah: Int,
    val detail_catat: List<DetailCatat>
)
