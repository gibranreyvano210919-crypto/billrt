package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TransaksiResponse(
    val status: Boolean,
    val message: String,
    val summary: TransaksiSummary,
    val data: List<TransaksiItem>
) : Serializable

data class TransaksiSummary(
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("total_nominal") val totalNominal: Float,
    @SerializedName("per_user_pencatat") val perUserPencatat: List<SummaryUserPencatat>? = null,
    @SerializedName("per_wilayah") val perWilayah: List<SummaryWilayahTransaksi>? = null
) : Serializable

data class SummaryUserPencatat(
    @SerializedName("id_user_pencatat") val idUserPencatat: Int,
    @SerializedName("nama_admin") val namaAdmin: String,
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("total_nominal") val totalNominal: Float
) : Serializable

data class SummaryWilayahTransaksi(
    @SerializedName("id_wilayah") val idWilayah: Int?,
    @SerializedName("nama_wilayah") val namaWilayah: String,
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("total_nominal") val totalNominal: Float
) : Serializable

data class TransaksiItem(
    @SerializedName("id_pembayaran") val idPembayaran: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    val username: String,
    val periode: String,
    @SerializedName("jam_bayar") val jamBayar: String,
    @SerializedName("tanggal_lengkap") val tanggalLengkap: String,
    val jumlah: Float,
    val metode: String,
    val admin: String, // Nama admin pencatat dari PHP
    val keterangan: String
) : Serializable
