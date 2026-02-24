package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MikrotikDashboardResponse(
    val status: Boolean,
    val data: MikrotikDashboardData?,
    val message: String?
) : Serializable

data class MikrotikDashboardData(
    val header: MikrotikHeader,
    val pppoe: MikrotikPppoe,
    val system: MikrotikSystem
) : Serializable

data class MikrotikHeader(
    @SerializedName("cpu_load") val cpuLoad: String,
    @SerializedName("board_name") val boardName: String,
    val identity: String,
    val version: String,
    val uptime: String,
    @SerializedName("free_mem") val freeMem: String? // <-- DITAMBAHKAN
) : Serializable

data class MikrotikPppoe(
    @SerializedName("total_secret") val totalSecret: Int,
    @SerializedName("total_active") val totalActive: Int,
    @SerializedName("non_active") val nonActive: Int
) : Serializable

data class MikrotikSystem(
    @SerializedName("total_queue") val totalQueue: Int,
    @SerializedName("total_arp") val totalArp: Int,
    @SerializedName("queue_nonaktif") val queueNonaktif: Int
) : Serializable
