package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.GenerateTagihanResponse
import com.linkbit.billrt.model.RekapJumlahPelanggan
import com.linkbit.billrt.model.RekapJumlahPelangganResponse
import com.linkbit.billrt.model.RekapTagihan
import com.linkbit.billrt.model.RekapTagihanResponse
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DashboardTagihanViewModel : ViewModel() {

    private val _rekapData = MutableLiveData<RekapTagihan?>()
    val rekapData: LiveData<RekapTagihan?> = _rekapData

    private val _rekapJumlah = MutableLiveData<RekapJumlahPelanggan?>()
    val rekapJumlah: LiveData<RekapJumlahPelanggan?> = _rekapJumlah

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _generateResult = MutableLiveData<Result<String>>()
    val generateResult: LiveData<Result<String>> = _generateResult

    fun fetchAllDashboardData(bulan: Int, tahun: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        _rekapData.value = null
        _rekapJumlah.value = null

        viewModelScope.launch {
            val rekapDeferred = async { ApiClient.tagihanApiService.getRekapTagihan(bulan, tahun) }
            val jumlahDeferred = async { ApiClient.tagihanApiService.getRekapJumlahPelanggan(bulan, tahun) }

            try {
                val rekapResponse = rekapDeferred.await()
                _rekapData.value = rekapResponse.data
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data rekap: ${e.message}"
            }

            try {
                val jumlahResponse = jumlahDeferred.await()
                _rekapJumlah.value = jumlahResponse.data
            } catch (e: Exception) {
                val currentError = _errorMessage.value
                _errorMessage.value = if (currentError != null) "$currentError\n${e.message}" else "Gagal mengambil jumlah pelanggan: ${e.message}"
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
                if (response.status) {
                    _generateResult.value = Result.success(response.message ?: "Generate tagihan berhasil")
                } else {
                    val errorMessage = response.message ?: "Gagal generate tagihan"
                    _generateResult.value = Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                _generateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
