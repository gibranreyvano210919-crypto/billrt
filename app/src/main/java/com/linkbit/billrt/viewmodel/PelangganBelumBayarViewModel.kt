package com.linkbit.billrt.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.PelangganBelumBayarItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class PelangganBelumBayarViewModel : ViewModel() {

    private val _pelangganList = MutableLiveData<List<PelangganBelumBayarItem>>()
    val pelangganList: LiveData<List<PelangganBelumBayarItem>> = _pelangganList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchPelangganBelumBayar(bulan: Int, tahun: Int, idWilayah: Int? = null, search: String? = null) {
        _isLoading.value = true
        val searchQuery = if (search.isNullOrBlank()) null else search
        Log.d("SearchDebug", "ViewModel Memulai fetch dengan pencarian: '$searchQuery'")
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getPelangganBelumBayar(bulan, tahun, idWilayah, searchQuery)
                if (response.status) {
                    Log.d("SearchDebug", "ViewModel Respon API berhasil.")
                    _pelangganList.value = response.data ?: emptyList()
                } else {
                    Log.e("SearchDebug", "ViewModel Respon API tidak berhasil")
                }
            } catch (e: Exception) {
                Log.e("SearchDebug", "ViewModel Panggilan API gagal", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
