package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SyncMikrotikListResponse(
    @SerializedName("status") val status: Any?,
    @SerializedName("total_data") val totalData: Int? = 0,
    @SerializedName("data") val data: List<PelangganSyncItem>?
) : Serializable {
    val isSuccess: Boolean
        get() = status == true || status == "success" || status == "true" || status == 1 || status == "1"
}

data class PelangganSyncItem(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("mac_address") var macAddress: String?,
    @SerializedName("static_ip") var staticIp: String?,
    
    // UI state properties
    var isSyncing: Boolean = false,
    var lastSyncStatus: String? = null,
    var isSuccess: Boolean? = null,
    var oldMacAddress: String? = null,
    var oldStaticIp: String? = null,
    var hasChanged: Boolean? = null
) : Serializable

data class SyncSingleData(
    @SerializedName("caller_id") val callerId: String?,
    @SerializedName("static_ip") val staticIp: String?
) : Serializable

data class SyncSingleResponse(
    @SerializedName("status") val status: Any?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: SyncSingleData?
) : Serializable {
    val isSuccess: Boolean
        get() = status == true || status == "success" || status == "true" || status == 1 || status == "1"
}
