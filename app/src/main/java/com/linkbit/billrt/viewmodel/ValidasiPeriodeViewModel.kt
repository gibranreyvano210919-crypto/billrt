package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.RekapTunggakanLanjutan
import com.linkbit.billrt.model.Teknisi
import com.linkbit.billrt.model.Wilayah
import com.linkbit.billrt.network.ApiResult
import com.linkbit.billrt.network.RetrofitClient
import kotlinx.coroutines.launch

class ValidasiPeriodeViewModel : ViewModel() {

    private val _wilayahList = MutableLiveData<ApiResult<List<Wilayah>>>()
    val wilayahList: LiveData<ApiResult<List<Wilayah>>> = _wilayahList

    private val _teknisiList = MutableLiveData<ApiResult<List<Teknisi>>>()
    val teknisiList: LiveData<ApiResult<List<Teknisi>>> = _teknisiList

    private val _rekapTunggakan = MutableLiveData<ApiResult<List<RekapTunggakanLanjutan>>>()
    val rekapTunggakan: LiveData<ApiResult<List<RekapTunggakanLanjutan>>> = _rekapTunggakan

    private val _periodeTarget = MutableLiveData<String>()
    val periodeTarget: LiveData<String> = _periodeTarget

    fun fetchWilayah() {
        _wilayahList.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getWilayah()
                _wilayahList.postValue(ApiResult.Success(response.data))
            } catch (e: Exception) {
                _wilayahList.postValue(ApiResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    fun fetchTeknisi() {
        _teknisiList.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getTeknisi()
                _teknisiList.postValue(ApiResult.Success(response.data))
            } catch (e: Exception) {
                _teknisiList.postValue(ApiResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    fun fetchRekapTunggakan(bulan: Int, tahun: Int, idWilayah: Int?, idTeknisi: Int?) {
        _rekapTunggakan.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRekapTunggakanLanjutan(bulan, tahun, idWilayah, idTeknisi)
                _rekapTunggakan.postValue(ApiResult.Success(response.data))
                _periodeTarget.postValue(response.periodeTarget)
            } catch (e: Exception) {
                _rekapTunggakan.postValue(ApiResult.Error(e.message ?: "An error occurred"))
            }
        }
    }
}