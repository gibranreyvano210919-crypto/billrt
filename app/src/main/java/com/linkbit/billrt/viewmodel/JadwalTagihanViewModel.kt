package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.TunggakanItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class JadwalTagihanViewModel : ViewModel() {

    private val _tunggakanList = MutableLiveData<List<TunggakanItem>>()
    val tunggakanList: LiveData<List<TunggakanItem>> get() = _tunggakanList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _summary = MutableLiveData<Pair<Int, Double>>()
    val summary: LiveData<Pair<Int, Double>> get() = _summary

    fun fetchTunggakanKumulatif(bulan: Int, tahun: Int, idWilayah: Int? = null, search: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getTunggakanKumulatif(bulan, tahun, idWilayah, search)
                if (response.status) {
                    _tunggakanList.value = response.data
                    _summary.value = Pair(response.summary.totalPelanggan, response.summary.grandTotal)
                } else {
                    _toastMessage.value = response.message
                    _tunggakanList.value = emptyList()
                }
            } catch (e: Exception) {
                _toastMessage.value = "Failed to load data: ${e.message}"
                _tunggakanList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
