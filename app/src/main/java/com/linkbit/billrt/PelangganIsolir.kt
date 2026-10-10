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
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("status_aktif") val statusAktif: String?,
    @SerializedName("nama_wilayah") val namaWilayah: String?,
    @SerializedName("tgl_isolir", alternate = ["tanggal_isolasi", "tgl_isolasi", "tgl_jatuh_tempo"]) val tglIsolir: String?,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?
)
