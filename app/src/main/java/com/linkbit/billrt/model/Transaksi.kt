package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class Transaksi(
    @SerializedName("id_pembayaran") val idPembayaran: Int,
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("username") val username: String?,
    @SerializedName("periode") val periode: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("jumlah") val jumlah: Float,
    @SerializedName("metode") val metode: String,
    @SerializedName("admin") val admin: String?,
    @SerializedName("keterangan") val keterangan: String?
)

data class TransaksiResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("filter") val filter: Filter,
    @SerializedName("summary") val summary: Summary,
    @SerializedName("data") val data: List<Transaksi>
)

data class Filter(
    @SerializedName("tgl_mulai") val tglMulai: String,
    @SerializedName("tgl_akhir") val tglAkhir: String,
    @SerializedName("admin_id") val adminId: Int
)

data class Summary(
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("total_nominal") val totalNominal: Float
)
