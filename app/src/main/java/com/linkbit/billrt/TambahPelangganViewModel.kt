package com.linkbit.billrt

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A Shared ViewModel to hold data across the fragments of the 'Add Customer' wizard.
 */
class TambahPelangganViewModel : ViewModel() {
    // Hidden fields
    val idPelanggan = MutableLiveData<String?>()

    // Step 1 Data
    val namaPelanggan = MutableLiveData<String>()
    val alamat = MutableLiveData<String>()
    val telepon = MutableLiveData<String?>()

    // Step 2 Data
    val idPaket = MutableLiveData<Int>()
    val idWilayah = MutableLiveData<Int>()
    val mikrotikUsername = MutableLiveData<String>()
    val mikrotikPassword = MutableLiveData<String?>()

    // Step 3 Data
    val macAddress = MutableLiveData<String?>()
    val installationDate = MutableLiveData<String?>().apply {
        // Set default to today
        value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val tglDaftar = MutableLiveData<String?>()
    val tglExpired = MutableLiveData<String?>()

    /**
     * Creates a SimpanPelangganRequest object from the data collected in the ViewModel.
     *
     * @return A SimpanPelangganRequest object, or null if required fields are missing.
     */
    fun createSaveRequest(): SimpanPelangganRequest? {
        val request = SimpanPelangganRequest(
            idPelanggan = idPelanggan.value,
            namaPelanggan = namaPelanggan.value ?: return null, // Required
            alamatPelanggan = alamat.value ?: return null, // Required
            teleponPelanggan = telepon.value,
            idPaket = idPaket.value ?: return null, // Required
            idWilayah = idWilayah.value ?: return null, // Required
            mikrotikUsername = mikrotikUsername.value ?: return null, // Required
            mikrotikPassword = mikrotikPassword.value,
            macAddress = macAddress.value,
            latitude = null, // Set to null as it's not collected here
            longitude = null, // Set to null as it's not collected here
            tglDaftar = tglDaftar.value,
            tglExpired = tglExpired.value,
            installationDate = installationDate.value
        )
        return request
    }
}
