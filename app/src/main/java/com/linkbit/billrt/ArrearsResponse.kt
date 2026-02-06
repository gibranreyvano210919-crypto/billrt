package com.linkbit.billrt

import com.google.gson.annotations.SerializedName

data class ArrearsResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("total_tunggakan")
    val totalTunggakan: Int,
    @SerializedName("data")
    val data: List<ArrearsRegionRecapFromApi>
)

data class ArrearsRegionRecapFromApi(
    @SerializedName("nama_wilayah")
    val namaWilayah: String,
    @SerializedName("jumlah")
    val jumlah: Int,
    @SerializedName("pelanggan")
    val pelanggan: List<ArrearsCustomerFromApi>
)

data class ArrearsCustomerFromApi(
    @SerializedName("id_pelanggan")
    val idPelanggan: String,
    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,
    @SerializedName("mikrotik_username")
    val mikrotikUsername: String
)
