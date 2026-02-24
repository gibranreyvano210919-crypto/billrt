package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PembayaranHariIni(
    @SerializedName("no") val no: Int,
    @SerializedName("id_pembayaran") val idPembayaran: Int,
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("periode") val periode: String,
    @SerializedName("jumlah") val jumlah: String,
    @SerializedName("metode") val metode: String,
    @SerializedName("waktu") val waktu: String
)
