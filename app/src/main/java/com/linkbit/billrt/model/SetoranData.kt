package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class EditCatatanSetoranRequest(
    @SerializedName("id_setoran")
    val idSetoran: Int,
    @SerializedName("catatan")
    val catatan: String
)

data class HapusSetoranRequest(
    @SerializedName("id_setoran")
    val idSetoran: Int
)
