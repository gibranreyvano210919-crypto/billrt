package com.linkbit.billrt.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PppoeProfileResponse(
    val status: Boolean,
    val data: List<PppoeProfile>?,
    val message: String?
) : Serializable

data class PppoeProfile(
    val name: String
) : Serializable

data class AvailableIpResponse(
    val status: Boolean,
    val data: List<AvailableIp>?,
    val message: String?
) : Serializable

data class AvailableIp(
    val ip: String
) : Serializable

data class AddPppoeRequest(
    @SerializedName("router_id") val routerId: Int,
    val user: String,
    val pass: String,
    val profile: String,
    val comment: String,
    @SerializedName("remote_ip") val remoteIp: String? = null
) : Serializable
