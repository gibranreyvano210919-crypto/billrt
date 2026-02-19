package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PelangganDetailResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PelangganDetail?
)
