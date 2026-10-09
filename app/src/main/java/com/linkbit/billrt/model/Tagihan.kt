package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import com.linkbit.billrt.SummaryWilayahItem
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
    val total: Int? = null,
    val data: List<PelangganBelumBayarItem>?,
    @SerializedName("summary_wilayah") val summaryWilayah: List<SummaryWilayahItem>? = null,
    @SerializedName("count_wilayah") val countWilayah: Map<String, Int>? = null
) : Serializable

data class PelangganBelumBayarItem(
    @SerializedName("id_pelanggan") override val id_pelanggan: Int,
    @SerializedName("id_wilayah") val idWilayah: Int? = null,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String? = null,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String? = null,
    val wilayah: String? = null,
    val invoice: String? = null,
    val nominal: Float = 0f,
    @SerializedName("status_pembayaran") val statusPembayaran: Int = 0,
    @SerializedName("status_text") val statusText: String? = null,
    @SerializedName("catatan_tagout") val catatanTagout: String? = null,
    @SerializedName("bulan_tagihan") val bulanTagihan: String? = null,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int? = null,
    @SerializedName("tgl_bayar_terakhir") val tglBayarTerakhir: String? = null,
    @SerializedName("nama_pencatat") val namaPencatat: String? = null,
    @SerializedName("performa_pembayaran") val performaPembayaran: String? = null,
    @SerializedName("performa_float") val performaFloat: Float? = null
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
    @SerializedName("jatuh_tempo", alternate = ["tanggal_isolasi", "tgl_isolasi", "tgl_jatuh_tempo"])
    val jatuh_tempo: String?,
    val tgl_bayar: String?
) : Serializable
