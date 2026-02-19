package com.linkbit.billrt.model

data class PembayaranHariIni(
    val no: Int,
    val id_pembayaran: Int,
    val id_tagihan: String,
    val nama_pelanggan: String,
    val periode: String,
    val jumlah: Float,
    val metode: String,
    val waktu: String
)
