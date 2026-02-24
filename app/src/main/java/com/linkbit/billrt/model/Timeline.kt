package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName

data class TimelineResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("profil")
    val profil: Profil?,
    @SerializedName("timeline")
    val timeline: List<TimelineItem>?
)

data class Profil(
    @SerializedName("id_pelanggan")
    val idPelanggan: Int,
    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,
    @SerializedName("wilayah")
    val wilayah: String,
    @SerializedName("installation_date")
    val installationDate: String,
    @SerializedName("paket")
    val paket: String
)

data class TimelineItem(
    @SerializedName("title")
    val title: String,
    @SerializedName("subtitle")
    val subtitle: String,
    @SerializedName("date_display")
    val dateDisplay: String,
    @SerializedName("status_text")
    val statusText: String,
    @SerializedName("dot_color")
    val dotColor: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("has_invoice")
    val hasInvoice: Boolean,
    @SerializedName("id_tagihan")
    val idTagihan: String?
)
