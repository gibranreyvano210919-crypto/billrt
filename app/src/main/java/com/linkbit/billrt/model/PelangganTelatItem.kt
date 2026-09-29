package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PelangganTelatItem(
    @SerializedName("id_pelanggan")
    val idPelanggan: Int,
    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,
    @SerializedName("mikrotik_username")
    val mikrotikUsername: String,
    @SerializedName("telepon")
    val telepon: String?,
    val wilayah: String,
    @SerializedName("periode_tagihan")
    val periodeTagihan: String,
    val nominal: Double,
    @SerializedName("tgl_jatuh_tempo")
    val tglJatuhTempo: Int,
    @SerializedName("tgl_bayar_terakhir")
    val tglBayarTerakhir: String,
    @SerializedName("id_user_pencatat")
    val idUserPencatat: Int?,
    @SerializedName("nama_pencatat")
    val namaPencatat: String,
    val keterangan: String
)
