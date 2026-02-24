package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PembayaranHariIniResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("total_item") val totalItem: Int,
    @SerializedName("total_nominal") val totalNominal: Float,
    @SerializedName("data") val data: List<PembayaranHariIni>
)
