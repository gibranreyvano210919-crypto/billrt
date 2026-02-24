package com.linkbit.billrt.model

data class PelangganTelatResponse(
    val status: Boolean,
    val message: String,
    val data: List<PelangganTelatItem>
)
