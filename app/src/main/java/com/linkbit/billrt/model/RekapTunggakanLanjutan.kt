package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class RekapTunggakanLanjutan(
    @SerializedName("id_pelanggan")
    val id_pelanggan: String,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("wilayah")
    val wilayah: String,

    @SerializedName("tgl_terakhir")
    val tgl_terakhir: String?,

    @SerializedName("teknisi_prev")
    val teknisi_prev: String?,

    @SerializedName("periode_lalu")
    val periode_lalu: String?
)
