package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.PeriodeTagihan
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class PeriodeTagihanViewModel : ViewModel() {

    private val _periodeList = MutableLiveData<List<PeriodeTagihan>?>()
    val periodeList: LiveData<List<PeriodeTagihan>?> = _periodeList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchPeriodeTagihan() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getListPeriode()
                if (response.status) {
                    _periodeList.value = response.data
                } else {
                    _errorMessage.value = "Gagal mengambil data periode"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
