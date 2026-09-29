package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.model.LunasSummary
import com.linkbit.billrt.model.PelangganLunasItem
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class LunasViewModel : ViewModel() {

    private val _originalList = MutableLiveData<List<PelangganLunasItem>>()
    
    private val _pelangganList = MutableLiveData<List<PelangganLunasItem>>()
    val pelangganList: LiveData<List<PelangganLunasItem>> = _pelangganList

    private val _summary = MutableLiveData<LunasSummary?>()
    val summary: LiveData<LunasSummary?> = _summary

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private var currentIdWilayah: Int? = null
    private var currentIdUserPencatat: Int? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null
    private var currentSearchQuery: String = ""
    
    private var lastBulan: Int = 0
    private var lastTahun: Int = 0

    fun fetchPelangganLunas(
        bulan: Int, 
        tahun: Int, 
        idWilayah: Int? = null, 
        idUserPencatat: Int? = null, 
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null
    ) {
        lastBulan = bulan
        lastTahun = tahun
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getPelangganLunas(
                    bulan, tahun, idWilayah, idUserPencatat, startDate, endDate, search
                )
                if (response.status) {
                    val list = response.data ?: emptyList()
                    _originalList.value = list
                    _summary.value = response.summary
                    _pelangganList.value = list
                } else {
                    _toastMessage.value = "Gagal mengambil data pelanggan lunas"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyFilter(idWil: Int?, idPencatat: Int?, start: String?, end: String?) {
        currentIdWilayah = idWil
        currentIdUserPencatat = idPencatat
        currentStartDate = start
        currentEndDate = end
        
        fetchPelangganLunas(lastBulan, lastTahun, idWil, idPencatat, start, end, currentSearchQuery)
    }

    // Fungsi lama untuk kompatibilitas jika ada yang memanggil
    fun filterByWilayah(id: Int?) {
        currentIdWilayah = id
        fetchPelangganLunas(lastBulan, lastTahun, id, currentIdUserPencatat, currentStartDate, currentEndDate, currentSearchQuery)
    }

    fun filterByPencatat(id: Int?) {
        currentIdUserPencatat = id
        fetchPelangganLunas(lastBulan, lastTahun, currentIdWilayah, id, currentStartDate, currentEndDate, currentSearchQuery)
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        fetchPelangganLunas(lastBulan, lastTahun, currentIdWilayah, currentIdUserPencatat, currentStartDate, currentEndDate, query)
    }

    fun resetFilter() {
        currentIdWilayah = null
        currentIdUserPencatat = null
        currentStartDate = null
        currentEndDate = null
        currentSearchQuery = ""
        fetchPelangganLunas(lastBulan, lastTahun, null, null, null, null, "")
    }

    fun batalPembayaran(idTagihan: String?, bulan: Int, tahun: Int, idWilayah: Int? = null) {
        if (idTagihan == null) {
            _toastMessage.value = "ID Tagihan tidak ditemukan, tidak dapat membatalkan."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.batalPembayaran(idTagihan)
                val message = response.message
                if (response.status) {
                    _toastMessage.value = message ?: "Pembayaran berhasil dibatalkan"
                    fetchPelangganLunas(bulan, tahun, currentIdWilayah, currentIdUserPencatat, currentStartDate, currentEndDate, currentSearchQuery)
                } else {
                    _toastMessage.value = message ?: "Gagal membatalkan pembayaran"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
