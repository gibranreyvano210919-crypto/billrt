package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.JadwalTagihanItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class JadwalTagihanViewModel : ViewModel() {

    private val _jadwalHariIniList = MutableLiveData<List<JadwalTagihanItem>>()
    val jadwalHariIniList: LiveData<List<JadwalTagihanItem>> get() = _jadwalHariIniList

    private val _jadwalBesokList = MutableLiveData<List<JadwalTagihanItem>>()
    val jadwalBesokList: LiveData<List<JadwalTagihanItem>> get() = _jadwalBesokList

    private val _isLoadingHariIni = MutableLiveData<Boolean>()
    val isLoadingHariIni: LiveData<Boolean> get() = _isLoadingHariIni

    private val _isLoadingBesok = MutableLiveData<Boolean>()
    val isLoadingBesok: LiveData<Boolean> get() = _isLoadingBesok

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun fetchJadwalTagihan(mode: String, idWilayah: Int? = null) {
        if (mode == "hari_ini") {
            _isLoadingHariIni.value = true
        } else {
            _isLoadingBesok.value = true
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getJadwalTagihan(mode, idWilayah)
                if (response.status) {
                    if (mode == "hari_ini") {
                        _jadwalHariIniList.value = response.data ?: emptyList()
                    } else {
                        _jadwalBesokList.value = response.data ?: emptyList()
                    }
                } else {
                    _toastMessage.value = response.message ?: ""
                }
            } catch (e: Exception) {
                _toastMessage.value = "Failed to load data: ${e.message}"
            } finally {
                if (mode == "hari_ini") {
                    _isLoadingHariIni.value = false
                } else {
                    _isLoadingBesok.value = false
                }
            }
        }
    }
}
