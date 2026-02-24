package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.MikrotikDashboardResponse
import kotlinx.coroutines.launch

class MikrotikMenuViewModel : ViewModel() {

    private val _dashboardData = MutableLiveData<MikrotikDashboardResponse?>()
    val dashboardData: LiveData<MikrotikDashboardResponse?> = _dashboardData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchDashboardData(routerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMikrotikDashboard(routerId)
                if (response.isSuccessful && response.body()?.status == true) {
                    _dashboardData.postValue(response.body())
                    _errorMessage.postValue(null)
                } else {
                    // PERBAIKAN FINAL: Sekarang aman untuk mengakses .message
                    val errorMsg = response.body()?.message ?: "Gagal memuat dashboard. Kode: ${response.code()}"
                    _errorMessage.postValue(errorMsg)
                    _dashboardData.postValue(null)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                _dashboardData.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
