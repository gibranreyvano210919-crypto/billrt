package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PelangganLunasResponse(
    val status: Boolean,
    val data: List<PelangganLunasItem>?,
    val summary: LunasSummary? = null
) : Serializable

data class LunasSummary(
    @SerializedName("total_data") val totalData: Int?,
    @SerializedName("count_wilayah") val countWilayah: Map<String, Int>?,
    @SerializedName("count_pencatat") val countPencatat: Map<String, Int>?
) : Serializable

data class PelangganLunasItem(
    @SerializedName("id_tagihan") val idTagihan: String?,
    @SerializedName("id_pelanggan") override val id_pelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    @SerializedName("id_wilayah") val idWilayah: Int?,
    val wilayah: String?,
    @SerializedName("nominal_tagihan") val nominalTagihan: Float,
    @SerializedName("jumlah_bayar") val jumlahBayar: Float,
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    @SerializedName("metode_bayar") val metodeBayar: String?,
    val keterangan: String?,
    @SerializedName("id_user_pencatat") val idUserPencatat: Int?,
    @SerializedName("nama_pencatat") val namaPencatat: String?,
    @SerializedName("status_aktif") val statusAktif: String?,
    @SerializedName("status_pembayaran") val statusPembayaran: Int,
    @SerializedName("bulan_tagihan") val bulanTagihan: Int,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int
) : Serializable, PelangganIdentifiable
