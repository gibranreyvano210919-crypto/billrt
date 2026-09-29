package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.PelangganSyncItem
import com.linkbit.billrt.model.SyncMikrotikListResponse
import com.linkbit.billrt.model.SyncSingleData
import com.linkbit.billrt.model.SyncSingleResponse
import com.linkbit.billrt.model.StandardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class SyncMikrotikViewModel : ViewModel() {

    private val _rawList = mutableListOf<PelangganSyncItem>()
    
    private val _filteredList = MutableLiveData<List<PelangganSyncItem>>()
    val filteredList: LiveData<List<PelangganSyncItem>> = _filteredList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isBatchSyncing = MutableLiveData<Boolean>(false)
    val isBatchSyncing: LiveData<Boolean> = _isBatchSyncing

    private val _batchProgressText = MutableLiveData<String>()
    val batchProgressText: LiveData<String> = _batchProgressText

    private val _batchProgressCurrent = MutableLiveData<Int>(0)
    val batchProgressCurrent: LiveData<Int> = _batchProgressCurrent

    private val _batchProgressTotal = MutableLiveData<Int>(0)
    val batchProgressTotal: LiveData<Int> = _batchProgressTotal

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private var currentSearchQuery = ""
    private var batchJob: Job? = null
    var activeRouterId: Int = 0

    fun fetchCustomerList(routerId: Int = 0) {
        if (routerId > 0) {
            activeRouterId = routerId
        }
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reqRouterId = activeRouterId.takeIf { it > 0 }
                var response: Response<SyncMikrotikListResponse>? = null
                try {
                    // 1. Try action = list_pelanggan with id_router and id
                    response = RetrofitInstance.api.getSyncPelangganList(action = "list_pelanggan", idRouter = reqRouterId, routerId = reqRouterId)
                } catch (_: Exception) {
                    try {
                        // 2. Try action = get_list
                        response = RetrofitInstance.api.getSyncPelangganList(action = "get_list", idRouter = reqRouterId, routerId = reqRouterId)
                    } catch (_: Exception) {
                        try {
                            response = RetrofitInstance.api.getSyncMikrotikList()
                        } catch (_: Exception) {
                            response = RetrofitInstance.api.getSyncMikrotikListAlt()
                        }
                    }
                }

                if (response != null && (!response.isSuccessful || response.body()?.isSuccess != true)) {
                    // Fallback to get_list
                    try {
                        val altResp = RetrofitInstance.api.getSyncPelangganList(action = "get_list", idRouter = reqRouterId, routerId = reqRouterId)
                        if (altResp.isSuccessful && altResp.body()?.isSuccess == true) {
                            response = altResp
                        }
                    } catch (_: Exception) {}
                }

                if (response != null && response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.data ?: emptyList()
                    _rawList.clear()
                    _rawList.addAll(list)
                    applyFilter()
                } else {
                    val errorMsg = "Gagal memuat daftar pelanggan (${response?.code() ?: "Error"})"
                    _errorMessage.postValue(errorMsg)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error koneksi: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun search(query: String) {
        currentSearchQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        val q = currentSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            _filteredList.postValue(ArrayList(_rawList))
        } else {
            val filtered = _rawList.filter { item ->
                item.namaPelanggan.lowercase().contains(q) ||
                        (item.mikrotikUsername?.lowercase()?.contains(q) == true) ||
                        (item.macAddress?.lowercase()?.contains(q) == true) ||
                        (item.staticIp?.lowercase()?.contains(q) == true) ||
                        item.idPelanggan.contains(q)
            }
            _filteredList.postValue(ArrayList(filtered))
        }
    }

    fun syncSingle(item: PelangganSyncItem, onComplete: (() -> Unit)? = null) {
        val username = item.mikrotikUsername
        if (username.isNullOrEmpty() || username == "-") {
            item.lastSyncStatus = "Username tidak valid"
            item.isSuccess = false
            item.hasChanged = false
            notifyItemUpdated(item)
            onComplete?.invoke()
            return
        }

        item.isSyncing = true
        item.lastSyncStatus = "Sedang Sync MikroTik..."
        item.isSuccess = null
        notifyItemUpdated(item)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reqRouterId = activeRouterId.takeIf { it > 0 }
                val oldMac = item.macAddress ?: "-"
                val oldIp = item.staticIp ?: "-"

                // Step 1: Sync Single from MikroTik (action = fetch_single)
                var syncResp: Response<SyncSingleResponse>? = null
                try {
                    syncResp = RetrofitInstance.api.syncSinglePelanggan(action = "fetch_single", username = username, idRouter = reqRouterId, routerId = reqRouterId)
                } catch (_: Exception) {
                    try {
                        syncResp = RetrofitInstance.api.syncSinglePelanggan(action = "sync_single", username = username, idRouter = reqRouterId, routerId = reqRouterId)
                    } catch (_: Exception) {
                        try {
                            syncResp = RetrofitInstance.api.syncSingleMikrotik(username = username)
                        } catch (_: Exception) {
                            syncResp = RetrofitInstance.api.syncSingleMikrotikAlt(username = username)
                        }
                    }
                }

                if (syncResp != null && (!syncResp.isSuccessful || syncResp.body()?.isSuccess != true)) {
                    try {
                        syncResp = RetrofitInstance.api.syncSinglePelanggan(action = "sync_single", username = username, idRouter = reqRouterId, routerId = reqRouterId)
                    } catch (_: Exception) {}
                }

                if (syncResp != null && syncResp.isSuccessful && syncResp.body()?.isSuccess == true) {
                    val syncData: SyncSingleData? = syncResp.body()?.data
                    val callerId = syncData?.callerId ?: "-"
                    val staticIp = syncData?.staticIp ?: "-"

                    val macChanged = oldMac.trim().uppercase() != callerId.trim().uppercase() && callerId != "-" && callerId.uppercase() != "UNKNOWN"
                    val ipChanged = oldIp.trim() != staticIp.trim() && staticIp != "-" && staticIp != "UNKNOWN"
                    val isDataChanged = macChanged || ipChanged

                    item.oldMacAddress = oldMac
                    item.oldStaticIp = oldIp
                    item.hasChanged = isDataChanged

                    item.lastSyncStatus = "Menyimpan ke DB..."
                    notifyItemUpdated(item)

                    // Step 2: Save to DB (action = save_single)
                    var saveResp: Response<StandardResponse>? = null
                    try {
                        saveResp = RetrofitInstance.api.saveSinglePelanggan(
                            action = "save_single",
                            idPelanggan = item.idPelanggan,
                            macBaru = callerId,
                            ipVal = staticIp
                        )
                    } catch (_: Exception) {
                        try {
                            saveResp = RetrofitInstance.api.saveSingleMikrotik(
                                idPelanggan = item.idPelanggan,
                                macBaru = callerId,
                                ipVal = staticIp
                            )
                        } catch (_: Exception) {
                            saveResp = RetrofitInstance.api.saveSingleMikrotikAlt(
                                idPelanggan = item.idPelanggan,
                                macBaru = callerId,
                                ipVal = staticIp
                            )
                        }
                    }

                    if (saveResp != null && saveResp.isSuccessful && saveResp.body()?.status == true) {
                        item.macAddress = callerId
                        item.staticIp = staticIp
                        item.isSuccess = true

                        if (isDataChanged) {
                            val changeDetail = when {
                                macChanged && ipChanged -> "Diperbarui:\n• MAC: $oldMac ➔ $callerId\n• IP: $oldIp ➔ $staticIp"
                                macChanged -> "Diperbarui:\n• MAC: $oldMac ➔ $callerId"
                                else -> "Diperbarui:\n• IP: $oldIp ➔ $staticIp"
                            }
                            item.lastSyncStatus = changeDetail
                        } else {
                            item.lastSyncStatus = "Sesuai (Tidak Ada Perubahan)"
                        }
                    } else {
                        val msg = saveResp?.body()?.message ?: "Gagal simpan ke DB"
                        item.lastSyncStatus = msg
                        item.isSuccess = false
                    }
                } else {
                    val errorMsg = syncResp?.body()?.message ?: "Gagal sync dari MikroTik"
                    item.lastSyncStatus = errorMsg
                    item.isSuccess = false
                    item.hasChanged = false
                }
            } catch (e: Exception) {
                item.lastSyncStatus = "Error: ${e.message}"
                item.isSuccess = false
                item.hasChanged = false
            } finally {
                item.isSyncing = false
                notifyItemUpdated(item)
                onComplete?.invoke()
            }
        }
    }

    fun syncAll() {
        if (_isBatchSyncing.value == true) return

        val validItems = _rawList.filter { 
            !it.mikrotikUsername.isNullOrEmpty() && it.mikrotikUsername != "-" 
        }

        if (validItems.isEmpty()) {
            _toastMessage.postValue("Tidak ada pelanggan dengan username MikroTik valid")
            return
        }

        _isBatchSyncing.value = true
        _batchProgressTotal.value = validItems.size
        _batchProgressCurrent.value = 0

        batchJob = viewModelScope.launch(Dispatchers.IO) {
            val total = validItems.size
            var changedCount = 0
            var unchangedCount = 0
            var failCount = 0

            for ((index, item) in validItems.withIndex()) {
                if (!batchJob!!.isActive) break

                val currentStep = index + 1
                _batchProgressCurrent.postValue(currentStep)
                _batchProgressText.postValue("Memproses $currentStep/$total: ${item.namaPelanggan}")

                val username = item.mikrotikUsername!!
                val oldMac = item.macAddress ?: "-"
                val oldIp = item.staticIp ?: "-"

                item.isSyncing = true
                item.lastSyncStatus = "Sedang Sync..."
                notifyItemUpdated(item)

                try {
                    val reqRouterId = activeRouterId.takeIf { it > 0 }
                    var syncResp: Response<SyncSingleResponse>? = null
                    try {
                        syncResp = RetrofitInstance.api.syncSinglePelanggan(action = "fetch_single", username = username, idRouter = reqRouterId, routerId = reqRouterId)
                    } catch (_: Exception) {
                        try {
                            syncResp = RetrofitInstance.api.syncSinglePelanggan(action = "sync_single", username = username, idRouter = reqRouterId, routerId = reqRouterId)
                        } catch (_: Exception) {
                            syncResp = RetrofitInstance.api.syncSingleMikrotik(username = username)
                        }
                    }

                    if (syncResp != null && syncResp.isSuccessful && syncResp.body()?.isSuccess == true) {
                        val syncData = syncResp.body()?.data
                        val callerId = syncData?.callerId ?: "-"
                        val staticIp = syncData?.staticIp ?: "-"

                        val macChanged = oldMac.trim().uppercase() != callerId.trim().uppercase() && callerId != "-" && callerId.uppercase() != "UNKNOWN"
                        val ipChanged = oldIp.trim() != staticIp.trim() && staticIp != "-" && staticIp != "UNKNOWN"
                        val isDataChanged = macChanged || ipChanged

                        item.oldMacAddress = oldMac
                        item.oldStaticIp = oldIp
                        item.hasChanged = isDataChanged

                        var saveResp: Response<StandardResponse>? = null
                        try {
                            saveResp = RetrofitInstance.api.saveSinglePelanggan(
                                action = "save_single",
                                idPelanggan = item.idPelanggan,
                                macBaru = callerId,
                                ipVal = staticIp
                            )
                        } catch (_: Exception) {
                            saveResp = RetrofitInstance.api.saveSingleMikrotik(
                                idPelanggan = item.idPelanggan,
                                macBaru = callerId,
                                ipVal = staticIp
                            )
                        }

                        if (saveResp != null && saveResp.isSuccessful && saveResp.body()?.status == true) {
                            item.macAddress = callerId
                            item.staticIp = staticIp
                            item.isSuccess = true

                            if (isDataChanged) {
                                val changeDetail = when {
                                    macChanged && ipChanged -> "Diperbarui:\n• MAC: $oldMac ➔ $callerId\n• IP: $oldIp ➔ $staticIp"
                                    macChanged -> "Diperbarui:\n• MAC: $oldMac ➔ $callerId"
                                    else -> "Diperbarui:\n• IP: $oldIp ➔ $staticIp"
                                }
                                item.lastSyncStatus = changeDetail
                                changedCount++
                            } else {
                                item.lastSyncStatus = "Sesuai (Tidak Ada Perubahan)"
                                unchangedCount++
                            }
                        } else {
                            item.lastSyncStatus = "Gagal Simpan DB"
                            item.isSuccess = false
                            failCount++
                        }
                    } else {
                        item.lastSyncStatus = syncResp?.body()?.message ?: "Gagal Sync"
                        item.isSuccess = false
                        item.hasChanged = false
                        failCount++
                    }
                } catch (e: Exception) {
                    item.lastSyncStatus = "Error: ${e.message}"
                    item.isSuccess = false
                    item.hasChanged = false
                    failCount++
                } finally {
                    item.isSyncing = false
                    notifyItemUpdated(item)
                }

                // Jeda delay 150ms antar siklus query
                delay(150)
            }

            _isBatchSyncing.postValue(false)
            _batchProgressText.postValue("Selesai: $changedCount Diperbarui, $unchangedCount Sesuai, $failCount Gagal")
        }
    }

    fun cancelBatchSync() {
        batchJob?.cancel()
        _isBatchSyncing.value = false
        _batchProgressText.value = "Batch Sync dibatalkan"
    }

    private fun notifyItemUpdated(item: PelangganSyncItem) {
        applyFilter()
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
