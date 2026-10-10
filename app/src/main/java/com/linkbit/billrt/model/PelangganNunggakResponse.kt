package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PelangganNunggakResponse(
    val status: Boolean,
    val message: String,
    val count: Int? = null,
    val data: List<PelangganNunggakItem>
) : Serializable

data class PelangganNunggakItem(
    @SerializedName("id_pelanggan") val id_pelanggan: String,
    @SerializedName("nama_pelanggan") val nama_pelanggan: String,
    @SerializedName("mikrotik_username") val mikrotik_username: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("nama_wilayah") val nama_wilayah: String,
    @SerializedName("total_tunggakan") val total_tunggakan: Float,
    @SerializedName("periode_nunggak") val periode_nunggak: String,
    @SerializedName("jatuh_tempo_tgl", alternate = ["tanggal_isolasi", "tgl_isolasi", "tgl_jatuh_tempo", "jatuh_tempo"]) val jatuh_tempo_tgl: Int,
    @SerializedName("tgl_bayar_terakhir") val tgl_bayar_terakhir: String,
    @SerializedName("admin_pencatat") val admin_pencatat: String
) : Serializable
