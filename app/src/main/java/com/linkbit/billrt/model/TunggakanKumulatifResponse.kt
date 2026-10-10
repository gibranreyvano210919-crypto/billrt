package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class TunggakanKumulatifResponse(
    val status: Boolean,
    val message: String,
    val filter: TunggakanFilter,
    val summary: TunggakanSummary,
    val data: List<TunggakanItem>
)

data class TunggakanFilter(
    val bulan: Int,
    val tahun: Int,
    @SerializedName("last_payment_before") val lastPaymentBefore: String
)

data class TunggakanSummary(
    @SerializedName("total_pelanggan") val totalPelanggan: Int,
    @SerializedName("grand_total") val grandTotal: Double
)

data class TunggakanItem(
    @SerializedName("id_pelanggan") val idPelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    val telepon: String?,
    val username: String,
    val wilayah: String,
    val paket: String,
    @SerializedName("harga_paket") val hargaPaket: Double,
    @SerializedName("periode_tunggakan") val periodeTunggakan: String,
    @SerializedName("jumlah_bulan") val jumlahBulan: Int,
    @SerializedName("total_nominal") val totalNominal: Double,
    @SerializedName("tgl_terakhir_bayar") val tglTerakhirBayar: String
)
