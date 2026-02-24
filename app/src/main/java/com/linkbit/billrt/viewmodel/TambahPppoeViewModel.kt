package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.AvailableIp
import com.linkbit.billrt.model.PppoeProfile
import kotlinx.coroutines.launch

class TambahPppoeViewModel : ViewModel() {

    private val _profiles = MutableLiveData<List<PppoeProfile>>()
    val profiles: LiveData<List<PppoeProfile>> = _profiles

    private val _availableIps = MutableLiveData<List<AvailableIp>>()
    val availableIps: LiveData<List<AvailableIp>> = _availableIps

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _addPppoeResult = MutableLiveData<Boolean>()
    val addPppoeResult: LiveData<Boolean> = _addPppoeResult

    fun fetchInitialData(routerId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Ambil data profil
                val profilesResponse = RetrofitInstance.api.getPppoeProfiles(routerId)
                if (profilesResponse.isSuccessful && profilesResponse.body()?.status == true) {
                    _profiles.postValue(profilesResponse.body()?.data ?: emptyList())
                } else {
                    _errorMessage.postValue("Gagal memuat profil: ${profilesResponse.body()?.message}")
                }

                // Ambil data IP yang tersedia
                val ipsResponse = RetrofitInstance.api.getAvailableIps(routerId, "pool1") // Ganti "pool1" jika perlu
                if (ipsResponse.isSuccessful && ipsResponse.body()?.status == true) {
                    _availableIps.postValue(ipsResponse.body()?.data ?: emptyList())
                } else {
                     _errorMessage.postValue("Gagal memuat IP: ${ipsResponse.body()?.message}")
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addPppoeSecret(
        routerId: Int,
        user: String,
        pass: String,
        profile: String,
        comment: String?,
        localIp: String?,
        remoteIp: String?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.addPppoeSecret(routerId, user, pass, profile, comment, localIp, remoteIp)
                if (response.isSuccessful && response.body()?.status == true) {
                    _addPppoeResult.postValue(true)
                } else {
                    _errorMessage.postValue(response.body()?.message ?: "Gagal menambah PPPoe")
                    _addPppoeResult.postValue(false)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
                _addPppoeResult.postValue(false)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
