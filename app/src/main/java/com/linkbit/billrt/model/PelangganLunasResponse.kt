package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PelangganLunasResponse(
    val status: Boolean,
    val data: List<PelangganLunasItem>?
) : Serializable

data class PelangganLunasItem(
    @SerializedName("id_tagihan") val idTagihan: String?,
    @SerializedName("id_pelanggan") override val id_pelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    val wilayah: String?,
    @SerializedName("nominal_tagihan") val nominalTagihan: Float,
    @SerializedName("jumlah_bayar") val jumlahBayar: Float,
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    @SerializedName("is_telat") val isTelat: Boolean,
    @SerializedName("metode_bayar") val metodeBayar: String?,
    val keterangan: String?,
    @SerializedName("status_aktif") val statusAktif: String?,
    @SerializedName("status_pembayaran") val statusPembayaran: Int
) : Serializable, PelangganIdentifiable
