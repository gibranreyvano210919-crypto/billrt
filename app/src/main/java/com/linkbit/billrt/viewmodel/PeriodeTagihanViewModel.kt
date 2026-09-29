package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.GenerateTagihanResponse
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

    private val _generateResult = MutableLiveData<GenerateTagihanResponse>()
    val generateResult: LiveData<GenerateTagihanResponse> = _generateResult

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

    fun generateTagihan(bulan: Int, tahun: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.generateTagihan(bulan, tahun)
                _generateResult.value = response
                if (response.status) {
                    fetchPeriodeTagihan() // Refresh list after generation
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan saat generate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
