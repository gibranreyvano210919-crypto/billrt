package com.linkbit.billrt

import androidx.lifecycle.ViewModel

class TambahPelangganViewModel : ViewModel() {
    // Properti untuk menyimpan data dari setiap langkah
    var namaPelanggan: String? = null
    var alamatPelanggan: String? = null
    var teleponPelanggan: String? = null
    var idPaket: Int? = null
    var idWilayah: Int? = null
    var installationDate: String? = null
    var mikrotikUsername: String? = null
    var mikrotikPassword: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var macAddress: String? = null
}
