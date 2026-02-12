package com.linkbit.billrt

import com.google.gson.annotations.SerializedName

data class PelangganIsolirResponse(
    val status: Boolean,
    val total: Int,
    val data: List<PelangganIsolir>
)

data class PelangganIsolir(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    @SerializedName("id_wilayah") val idWilayah: String?,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("installation_date") val installationDate: String?,
    @SerializedName("tgl_daftar") val tglDaftar: String?
)
