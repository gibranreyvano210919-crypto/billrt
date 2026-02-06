package com.linkbit.billrt

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Model data untuk item notifikasi dari Firebase.
 * Ini adalah satu-satunya sumber kebenaran untuk model notifikasi.
 */
@IgnoreExtraProperties
data class NotificationItem(
    val router_id: Int = 0,
    val title: String = "",
    val message: String = "", // Sesuai dengan Node.js
    val time: String = ""      // Sesuai dengan Node.js
)
