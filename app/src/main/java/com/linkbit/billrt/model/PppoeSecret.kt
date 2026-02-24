package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PppoeSecretResponse(
    val status: Boolean,
    val data: List<PppoeSecret>,
    val message: String?
) : Serializable

data class PppoeSecret(
    @SerializedName(".id") val id: String,
    val name: String,
    val profile: String,
    val comment: String,
    val disabled: String // "true" or "false"
) : Serializable
