package com.linkbit.billrt

data class UpdatePelangganRequest(
    val id_pelanggan: String,
    val nama_pelanggan: String,
    val alamat_pelanggan: String,
    val telepon_pelanggan: String,
    val id_paket: Int,
    val id_wilayah: Int,
    val mikrotik_username: String,
    val mikrotik_password: String?,
    val tgl_daftar: String,
    val installation_date: String,
    val tgl_expired: String?
)