package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class Params(
    @SerializedName("filter_kosong") val filterKosong: Boolean,
    @SerializedName("cek_bulan") val cekBulan: String,
    @SerializedName("cek_tahun") val cekTahun: String
)
