package com.linkbit.billrt

import com.google.gson.annotations.SerializedName

data class PelangganNonaktifResponse(
    val status: Boolean,
    val total: Int,
    val data: List<PelangganNonaktif>
)

data class PelangganNonaktif(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("status_aktif") val statusAktif: String?,
    @SerializedName("nama_wilayah") val namaWilayah: String?,
    @SerializedName("tgl_nonaktif") val tglNonaktif: String?,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?
)
