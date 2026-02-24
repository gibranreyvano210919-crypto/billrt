package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.PelangganTelatItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class PelangganTelatViewModel : ViewModel() {

    private val _pelangganList = MutableLiveData<List<PelangganTelatItem>>()
    val pelangganList: LiveData<List<PelangganTelatItem>> get() = _pelangganList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun fetchPelangganTelat(bulan: Int, tahun: Int, search: String? = null, idWilayah: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getPelangganTelat(bulan, tahun, search, idWilayah)
                if (response.status) {
                    _pelangganList.value = response.data
                } else {
                    _toastMessage.value = response.message
                }
            } catch (e: Exception) {
                _toastMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
