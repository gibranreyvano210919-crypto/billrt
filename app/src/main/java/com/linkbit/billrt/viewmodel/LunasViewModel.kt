package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.PelangganLunasItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class LunasViewModel : ViewModel() {

    private val _pelangganList = MutableLiveData<List<PelangganLunasItem>>()
    val pelangganList: LiveData<List<PelangganLunasItem>> = _pelangganList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    fun fetchPelangganLunas(bulan: Int, tahun: Int, search: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getPelangganLunas(bulan, tahun, search)
                if (response.status) {
                    _pelangganList.value = response.data ?: emptyList()
                } else {
                    _toastMessage.value = "Gagal mengambil data pelanggan lunas"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancels a payment.
     * @param idTagihan The ID of the billing to cancel. Made nullable to prevent crashes from the API.
     * @param bulan The billing month, used to refresh the list after cancellation.
     * @param tahun The billing year, used to refresh the list after cancellation.
     */
    fun batalPembayaran(idTagihan: String?, bulan: Int, tahun: Int) {
        // Guard against null idTagihan to prevent crashes.
        if (idTagihan == null) {
            _toastMessage.value = "ID Tagihan tidak ditemukan, tidak dapat membatalkan."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.batalPembayaran(idTagihan)
                val message = response.message
                if (response.status) {
                    _toastMessage.value = message ?: "Pembayaran berhasil dibatalkan"
                    // Refresh the list upon successful cancellation
                    fetchPelangganLunas(bulan, tahun)
                } else {
                    _toastMessage.value = message ?: "Gagal membatalkan pembayaran"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
