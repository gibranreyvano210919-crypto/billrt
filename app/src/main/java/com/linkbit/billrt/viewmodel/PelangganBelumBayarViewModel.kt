package com.linkbit.billrt.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.SummaryWilayahItem
import com.linkbit.billrt.model.PelangganBelumBayarItem
import com.linkbit.billrt.model.StandardResponse
import com.linkbit.billrt.network.ApiClient
import kotlinx.coroutines.launch

class PelangganBelumBayarViewModel : ViewModel() {

    private val _pelangganList = MutableLiveData<List<PelangganBelumBayarItem>>()
    val pelangganList: LiveData<List<PelangganBelumBayarItem>> = _pelangganList

    private val _summaryWilayah = MutableLiveData<List<SummaryWilayahItem>>()
    val summaryWilayah: LiveData<List<SummaryWilayahItem>> = _summaryWilayah

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isolirResult = MutableLiveData<StandardResponse>()
    val isolirResult: LiveData<StandardResponse> = _isolirResult

    // Cache Mapping Nama -> ID Wilayah (Gunakan key yang sudah di-trim dan lowercase untuk keandalan)
    private val wilayahIdMap = mutableMapOf<String, String>()
    private var masterWilayahList: List<SummaryWilayahItem>? = null

    private var lastBulan: Int = 0
    private var lastTahun: Int = 0
    private var currentIdWilayah: Int? = null
    private var currentSearchQuery: String? = null

    fun fetchPelangganBelumBayar(bulan: Int, tahun: Int, idWilayah: Int? = null, search: String? = null) {
        this.lastBulan = bulan
        this.lastTahun = tahun
        this.currentIdWilayah = idWilayah
        this.currentSearchQuery = if (search.isNullOrBlank()) null else search

        _isLoading.value = true
        Log.d("SearchDebug", "VM Fetching -> idWilayah: $idWilayah, search: $currentSearchQuery")
        
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.getPelangganBelumBayar(bulan, tahun, idWilayah, currentSearchQuery)
                if (response.status) {
                    val data = response.data ?: emptyList()
                    _pelangganList.value = data
                    
                    // 1. Simpan mapping Nama -> ID dari data pelanggan yang masuk
                    data.forEach { item ->
                        val wName = item.wilayah?.trim()?.lowercase()
                        if (!wName.isNullOrEmpty() && item.idWilayah != null) {
                            wilayahIdMap[wName] = item.idWilayah.toString()
                        }
                    }
                    
                    // 2. Olah count_wilayah (Map dari PHP) menjadi List UI
                    val currentSummary = response.countWilayah?.map { (nama, total) ->
                        val trimmedName = nama.trim()
                        // Cari ID di map menggunakan key lowercase
                        val id = wilayahIdMap[trimmedName.lowercase()] ?: ""
                        SummaryWilayahItem(id, trimmedName, total)
                    } ?: emptyList()

                    // 3. Simpan sebagai master list jika ini load awal (tanpa filter/search)
                    if (idWilayah == null && currentSearchQuery == null && currentSummary.isNotEmpty()) {
                        masterWilayahList = currentSummary
                    }
                    
                    // Kirim ke UI: Prioritaskan master list agar filter tetap lengkap
                    _summaryWilayah.value = masterWilayahList ?: currentSummary
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyFilter(idWil: Int?) {
        this.currentIdWilayah = idWil
        fetchPelangganBelumBayar(lastBulan, lastTahun, idWil, currentSearchQuery)
    }

    fun resetFilter() {
        currentIdWilayah = null
        currentSearchQuery = null
        if (lastBulan != 0) {
            fetchPelangganBelumBayar(lastBulan, lastTahun, null, null)
        }
    }

    fun isolirPelanggan(idPelanggan: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.tagihanApiService.isolirPelanggan(idPelanggan)
                _isolirResult.value = response
            } catch (e: Exception) {
                _isolirResult.value = StandardResponse(false, "Terjadi kesalahan: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
