package com.linkbit.billrt.model

data class Teknisi(
    val id: Int,
    val nama: String
) {
    override fun toString(): String {
        return nama
    }
}