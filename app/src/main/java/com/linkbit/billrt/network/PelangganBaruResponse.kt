package com.linkbit.billrt.network

import com.google.gson.annotations.SerializedName

data class PelangganBaruResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("total")
    val total: Int,

    @SerializedName("data")
    val data: List<PelangganBaru>
)

data class PelangganBaru(
    @SerializedName("id_pelanggan")
    val idPelanggan: String,

    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,

    @SerializedName("installation_date")
    val installationDate: String?,

    @SerializedName("telepon_pelanggan")
    val teleponPelanggan: String?,

    @SerializedName("tgl_daftar")
    val tglDaftar: String,

    @SerializedName("status_aktif")
    val statusAktif: String,

    @SerializedName("nama_wilayah")
    val namaWilayah: String?
)
