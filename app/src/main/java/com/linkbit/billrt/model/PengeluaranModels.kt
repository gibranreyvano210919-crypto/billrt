package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MasterKategori(
    val id: Int,
    @SerializedName("nama_kategori") val namaKategori: String,
    @SerializedName("kode_kategori") val kodeKategori: String,
    val keterangan: String?
) : Serializable

data class MasterKategoriResponse(
    val status: Boolean,
    val data: List<MasterKategori>
) : Serializable

data class PengeluaranItem(
    @SerializedName("id_pengeluaran") val idPengeluaran: Int,
    val tanggal: String,
    val keterangan: String,
    val kategori: String,
    @SerializedName("id_kategori_master") val idKategoriMaster: Int?,
    @SerializedName("kode_kategori") val kodeKategori: String,
    val jumlah: Double,
    @SerializedName("metode_pembayaran") val metodePembayaran: String,
    @SerializedName("keterangan_lain") val keteranganLain: String
) : Serializable

data class PengeluaranSummary(
    val bulan: Int,
    val tahun: Int,
    @SerializedName("total_nominal") val totalNominal: Double
) : Serializable

data class PengeluaranResponse(
    val status: Boolean,
    val summary: PengeluaranSummary?,
    val data: List<PengeluaranItem>?
) : Serializable
