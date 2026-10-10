package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PembayaranHariIni(
    @SerializedName("no") val no: Int,
    @SerializedName("id_pembayaran") val idPembayaran: Int,
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("id_user_pencatat") val idUserPencatat: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("periode") val periode: String,
    @SerializedName("jumlah") val jumlah: Double,
    @SerializedName("metode") val metode: String,
    @SerializedName("tanggal_admin") val tanggalAdmin: String,
    @SerializedName("waktu_sistem") val waktuSistem: String
)
