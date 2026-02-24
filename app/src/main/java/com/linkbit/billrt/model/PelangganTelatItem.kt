package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PelangganTelatItem(
    @SerializedName("id_pelanggan")
    val idPelanggan: Int,
    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,
    @SerializedName("mikrotik_username")
    val mikrotikUsername: String,
    val telepon: String,
    val wilayah: String,
    @SerializedName("periode_tagihan")
    val periodeTagihan: String,
    val nominal: Double,
    @SerializedName("tgl_tagih")
    val tglTagih: Int,
    @SerializedName("tgl_bayar_terakhir")
    val tglBayarTerakhir: String,
    @SerializedName("periode_bayar_terakhir")
    val periodeBayarTerakhir: String,
    val keterangan: String,
    val keterangan2: String
)
