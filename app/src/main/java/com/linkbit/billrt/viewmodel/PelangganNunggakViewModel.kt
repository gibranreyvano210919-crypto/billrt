package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.PelangganNunggakItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class PelangganNunggakViewModel : ViewModel() {

    private val _pelangganList = MutableLiveData<List<PelangganNunggakItem>>()
    val pelangganList: LiveData<List<PelangganNunggakItem>> get() = _pelangganList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _responseMessage = MutableLiveData<String>()
    val responseMessage: LiveData<String> get() = _responseMessage

    fun fetchPelangganNunggak(bulan: Int, tahun: Int, search: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getPelangganNunggak(bulan, tahun, search)
                _responseMessage.value = response.message
                if (response.status) {
                    _pelangganList.value = response.data
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
