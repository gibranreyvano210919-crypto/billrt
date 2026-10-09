package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class JadwalTagihanResponse(
    val status: Boolean,
    val mode: String,
    @SerializedName("tanggal_target") val tanggalTarget: Int,
    @SerializedName("total_data") val totalData: Int,
    val message: String?,
    val data: List<JadwalTagihanItem>?
)

data class JadwalTagihanItem(
    @SerializedName("id_pelanggan") val idPelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String,
    val telepon: String?,
    val wilayah: String,
    val paket: String,
    val nominal: Float,
    @SerializedName("tgl_jatuh_tempo", alternate = ["tanggal_isolasi", "tgl_isolasi", "jatuh_tempo_tgl"]) val tglJatuhTempo: Int,
    @SerializedName("tgl_terakhir_bayar") val tglTerakhirBayar: String
)
