package com.linkbit.billrt

data class UpdatePelangganRequest(
    val id_pelanggan: String,
    val nama_pelanggan: String? = null,
    val alamat_pelanggan: String? = null,
    val telepon_pelanggan: String? = null,
    val id_wilayah: Int? = null,
    val id_paket: Int? = null,
    val mikrotik_username: String? = null,
    val mikrotik_password: String? = null,
    val installation_date: String? = null
)
