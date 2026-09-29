package com.linkbit.billrt.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TransaksiLain(
    val id: Long = 0L,
    val tanggal: String? = null,
    val kategori: String = "",
    val jumlah: Double = 0.0,
    val keterangan: String = ""
)
