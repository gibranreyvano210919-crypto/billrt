package com.linkbit.billrt

import com.google.gson.annotations.SerializedName

// Dedicated data classes for the new flat-list RiwayatKasFragment to avoid any conflicts.

data class RiwayatKasListResponse(
    val status: Boolean,
    val message: String,
    val data: List<RiwayatKasItem>
)

data class RiwayatKasItem(
    val id: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("tanggal_catat") val tanggalCatat: String,
    @SerializedName("nama_teknisi") val namaTeknisi: String? // Can be null
)
