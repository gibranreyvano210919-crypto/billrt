package com.linkbit.billrt

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notifikasi(
    val title: String? = null,
    val message: String? = null,
    val timestamp: Long? = null
)
