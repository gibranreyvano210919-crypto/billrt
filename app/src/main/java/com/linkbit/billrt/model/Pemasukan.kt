package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PemasukanItem(
    @SerializedName("noUrut") val noUrut: Int,
    @SerializedName("id_pembayaran") val idPembayaran: Int,
    @SerializedName("namaPelanggan") val namaPelanggan: String,
    @SerializedName("jumlahBayar") val jumlahBayar: Double,
    @SerializedName("namaAdmin") val namaAdmin: String,
    @SerializedName("periodeTagihan") val periodeTagihan: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("id_user_pencatat") val idUserPencatat: Int
) : Serializable

data class PemasukanGroup(
    @SerializedName("tanggalBayar") val tanggalBayar: String,
    @SerializedName("jumlahPelanggan") val jumlahPelanggan: Int,
    @SerializedName("totalPerTanggal") val totalPerTanggal: Double,
    val rincian: List<PemasukanItem>
) : Serializable

data class PemasukanSummary(
    @SerializedName("totalPemasukan") val totalPemasukan: Double,
    @SerializedName("totalTransaksi") val totalTransaksi: Int,
    @SerializedName("jumlahPelanggan") val jumlahPelanggan: Int
) : Serializable

data class PemasukanFilter(
    val bulan: Int,
    val tahun: Int,
    @SerializedName("id_user_pencatat") val idUserPencatat: Int,
    val search: String
) : Serializable

data class PemasukanResponse(
    val status: Boolean,
    val message: String,
    val filter: PemasukanFilter,
    val summary: PemasukanSummary,
    val data: List<PemasukanGroup>
) : Serializable
