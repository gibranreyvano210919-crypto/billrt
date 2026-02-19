package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CustomerResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("total_data")
    val totalData: Int,
    @SerializedName("search_key")
    val searchKey: String,
    @SerializedName("data")
    val data: List<Customer>
)

data class Customer(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nama")
    val nama: String,
    @SerializedName("user_mk")
    val userMk: String,
    @SerializedName("tgl")
    val tgl: String,
    @SerializedName("wil")
    val wil: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("telp")
    val telp: String,
    @SerializedName("status_tagihan")
    val statusTagihan: String,
    @SerializedName("bulan_angka")
    val bulanAngka: List<String>
) : Serializable
