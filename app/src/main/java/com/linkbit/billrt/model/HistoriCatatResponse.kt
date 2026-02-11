package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class HistoriCatatResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("filter_aktif") val filterAktif: String,
    @SerializedName("total_data") val totalData: Int,
    @SerializedName("data") val data: List<HistoriCatat>
)
