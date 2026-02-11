package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class Wilayah(
    @SerializedName("id")
    val id_wilayah: Int,
    @SerializedName("nama")
    val nama_wilayah: String
) {
    override fun toString(): String {
        return nama_wilayah
    }
}