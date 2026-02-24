package com.linkbit.billrt

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

/**
 * Model data untuk item notifikasi dari Firebase.
 * Ini adalah satu-satunya sumber kebenaran untuk model notifikasi.
 */
@IgnoreExtraProperties
data class NotificationItem(
    @get:PropertyName("router_id") @set:PropertyName("router_id") var router_id: Int = 0,
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("message") @set:PropertyName("message") var message: String = "", // Sesuai dengan Node.js
    @get:PropertyName("time") @set:PropertyName("time") var time: String = ""      // Sesuai dengan Node.js
)
