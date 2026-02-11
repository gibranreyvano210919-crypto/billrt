package com.linkbit.billrt.model

data class DetailCatat(
    val tanggal: String,
    val teknisi_id: Int,
    val nama_teknisi: String?,
    val status_entry: String,
    val is_telat: Int = 0
)
