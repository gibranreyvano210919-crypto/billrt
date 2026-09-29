package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.PppoeOnlineUser
import kotlinx.coroutines.launch

class PelangganOnlineViewModel : ViewModel() {

    private val _users = MutableLiveData<List<PppoeOnlineUser>>()
    val users: LiveData<List<PppoeOnlineUser>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun fetchOnlineUsers(routerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPppoeOnline(routerId)
                if (response.isSuccessful && response.body()?.status == true) {
                    _users.postValue(response.body()?.data ?: emptyList())
                } else {
                    _errorMessage.postValue(response.body()?.message ?: "Gagal memuat data")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun kickUser(routerId: Int, mikrotikUsername: String?) {
        if (mikrotikUsername == null) {
            _toastMessage.postValue("Gagal: Username Mikrotik tidak ditemukan")
            return
        }
        
        viewModelScope.launch {
            try {
                // Sesuai ApiService.kt, parameter yang digunakan adalah user_id 
                // namun nilainya diisi dengan mikrotikUsername
                val response = RetrofitInstance.api.kickUser(routerId, mikrotikUsername)
                if (response.isSuccessful && response.body()?.status == true) {
                    _toastMessage.postValue("User $mikrotikUsername berhasil di-kick")
                    fetchOnlineUsers(routerId) // Refresh list
                } else {
                    _toastMessage.postValue(response.body()?.message ?: "Gagal kick user")
                }
            } catch (e: Exception) {
                _toastMessage.postValue("Error: ${e.message}")
            }
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}
