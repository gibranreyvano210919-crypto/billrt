package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PembayaranHariIniResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("mode") val mode: String?,
    @SerializedName("summary") val summary: Summary?,
    @SerializedName("data") val data: List<PembayaranHariIni>
)

data class Summary(
    @SerializedName("total_item") val totalItem: Int,
    @SerializedName("total_nominal") val totalNominal: Double,
    @SerializedName("per_user_pencatat") val perUserPencatat: List<PerUserPencatat>?
)

data class PerUserPencatat(
    @SerializedName("id_user_pencatat") val idUserPencatat: Int,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("total_nominal") val totalNominal: Double
)
