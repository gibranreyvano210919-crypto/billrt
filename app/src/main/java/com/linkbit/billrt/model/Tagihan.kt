package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PeriodeTagihan(
    @SerializedName("bulan_tagihan") val bulanTagihan: String,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int
) : Serializable

data class ListPeriodeResponse(
    val status: Boolean,
    val data: List<PeriodeTagihan>?
) : Serializable

data class Periode(
    val bulan: Int,
    val tahun: Int
) : Serializable

// --- Model untuk Daftar Pelanggan Belum Bayar ---
data class PelangganBelumBayarResponse(
    val status: Boolean,
    val filter: String?,
    val periode: Periode?,
    @SerializedName("total_penunggak")
    val totalPenunggak: Int,
    val data: List<PelangganBelumBayarItem>?
) : Serializable

data class PelangganBelumBayarItem(
    @SerializedName("id_pelanggan") override val id_pelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    val wilayah: String?,
    val invoice: String?,
    val nominal: Float,
    @SerializedName("status_aktif") val statusAktif: String?,
    @SerializedName("status_pembayaran") val statusPembayaran: Int,
    @SerializedName("bulan_tagihan") val bulanTagihan: Int?,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int?
) : Serializable, PelangganIdentifiable


// --- Model untuk Request Pembayaran ---
data class BayarTagihanRequest(
    val action: String = "bayar_tagihan",
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("id_pelanggan") val idPelanggan: Int,
    @SerializedName("jumlah_bayar") val jumlahBayar: Float,
    @SerializedName("tanggal_bayar") val tanggalBayar: String // Format YYYY-MM-DD
) : Serializable

// --- Model untuk Detail Tagihan (jika diperlukan nanti) ---
data class TagihanResponse(
    val status: Boolean,
    val total: Int,
    val data: List<TagihanItem>?
) : Serializable

data class TagihanItem(
    val id_tagihan: String,
    val id_pelanggan: Int,
    val nama: String,
    val jumlah: Double,
    val status: String,
    val jatuh_tempo: String?,
    val tgl_bayar: String?
) : Serializable
