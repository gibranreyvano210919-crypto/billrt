package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class RekapJumlahPelangganResponse(
    val status: Boolean,
    val data: RekapJumlahPelanggan
)

data class RekapJumlahPelanggan(
    @SerializedName("total_pelanggan_aktif")
    val totalPelangganAktif: Int,
    @SerializedName("sudah_generate")
    val sudahGenerate: Int,
    @SerializedName("belum_generate")
    val belumGenerate: Int,
    val lunas: Int,
    @SerializedName("belum_bayar")
    val belumBayar: Int
)
