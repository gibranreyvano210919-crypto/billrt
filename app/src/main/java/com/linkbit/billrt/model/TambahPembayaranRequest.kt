package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TambahPembayaranRequest(
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("metode_bayar") val metodeBayar: String,
    @SerializedName("id_user") val idUser: Int, // Diubah dari admin_id menjadi id_user agar sesuai PHP
    @SerializedName("tanggal_bayar") val tanggalBayar: String? = null,
    val keterangan: String? = null
) : Serializable
