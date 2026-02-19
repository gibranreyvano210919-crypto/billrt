package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

// Model baru yang sesuai dengan respons JSON dari API
data class HistoryPembayaranApiResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("data") val data: List<Tagihan>
)

// Model untuk setiap item dalam `data`
data class Tagihan(
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("bulan_tagihan") val bulanTagihan: String,
    @SerializedName("tahun_tagihan") val tahunTagihan: String,
    @SerializedName("jumlah_tagihan") val jumlahTagihan: Float,
    @SerializedName("status_tagihan") val statusTagihan: Int,
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    @SerializedName("metode_bayar") val metodeBayar: String?,
    @SerializedName("bulan_nama") val bulanNama: String,
    @SerializedName("harga_paket") val hargaPaket: Float,
    @SerializedName("nama_paket") val namaPaket: String
)

// Model-model ini tetap digunakan untuk menampilkan data di UI
data class PaymentSummary(
    val tahun: Int,
    val totalTagihanCount: Int,
    val lunasCount: Int,
    val totalNominalTagihan: Double,
    val totalNominalBayar: Double,
    val persentaseLunas: Double
)

data class PaymentDetail(
    val idTagihan: String,
    val tahun: Int,
    val bulanTagihan: Int, // Ditambahkan untuk sorting
    val bulanNama: String,
    val jumlahTagihan: Double,
    val status: Int, // 1 for Lunas, 0 for Belum Lunas
    val tglBayar: String,
    val metode: String,
    val namaPaket: String,
    val hargaPaket: Double
)
