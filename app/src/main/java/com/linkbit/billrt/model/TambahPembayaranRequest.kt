package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TambahPembayaranRequest(
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("metode_bayar") val metodeBayar: String,
    @SerializedName("admin_id") val adminId: Int,
    @SerializedName("tanggal_bayar") val tanggalBayar: String? = null,
    val keterangan: String? = null
) : Serializable
