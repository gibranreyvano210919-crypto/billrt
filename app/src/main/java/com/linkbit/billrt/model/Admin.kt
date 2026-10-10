package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AdminResponse(
    val status: Boolean,
    val data: List<AdminItem>
) : Serializable

data class AdminItem(
    @SerializedName("id") val idUser: Int, // Diselaraskan dengan "id" dari PHP
    @SerializedName("nama_lengkap") val namaLengkap: String
) : Serializable
