package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PelangganTelatTahunanSummary(
    @SerializedName("total_orang") val totalOrang: Int
) : Serializable

data class PelangganTelatTahunanItem(
    @SerializedName("id_pelanggan") val idPelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    val telepon: String,
    val wilayah: String,
    @SerializedName("jumlah_bulan_telat") val jumlahBulanTelat: Int,
    @SerializedName("list_periode") val listPeriode: String
) : Serializable

data class PelangganTelatTahunanResponse(
    val status: Boolean,
    val message: String,
    val summary: PelangganTelatTahunanSummary?,
    val data: List<PelangganTelatTahunanItem>
) : Serializable
