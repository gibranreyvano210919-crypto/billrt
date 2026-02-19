package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class RekapTagihanResponse(
    val status: Boolean,
    val data: RekapTagihan
)

data class RekapTagihan(
    val lunas: RekapItem,
    @SerializedName("belum_bayar")
    val belumBayar: RekapItem
)

data class RekapItem(
    @SerializedName("total_uang")
    val totalUang: Float?,
    @SerializedName("total_user")
    val totalUser: Int?
)
