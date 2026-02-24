package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class PelangganNunggakResponse(
    val status: Boolean,
    val message: String,
    val data: List<PelangganNunggakItem>
)

data class PelangganNunggakItem(
    @SerializedName("id_pelanggan") val id_pelanggan: String,
    @SerializedName("nama_pelanggan") val nama_pelanggan: String,
    @SerializedName("mikrotik_username") val mikrotik_username: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("installation_date") val installation_date: String,
    @SerializedName("nama_wilayah") val nama_wilayah: String,
    @SerializedName("total_tunggakan") val total_tunggakan: Float,
    @SerializedName("periode") val periode: String
)
