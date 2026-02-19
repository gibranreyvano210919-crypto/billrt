package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.PelangganDetail
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class CustomerDetailViewModel : ViewModel() {

    private val _customerDetail = MutableLiveData<PelangganDetail?>()
    val customerDetail: LiveData<PelangganDetail?> = _customerDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchCustomerDetail(pelangganId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getDetailPelangganNew(pelangganId)
                if (response.status) {
                    _customerDetail.value = response.data
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
