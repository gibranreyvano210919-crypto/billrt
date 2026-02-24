package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.PppoeOfflineUser
import kotlinx.coroutines.launch

class PelangganOfflineViewModel : ViewModel() {

    private val _users = MutableLiveData<List<PppoeOfflineUser>>()
    val users: LiveData<List<PppoeOfflineUser>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchOfflineUsers(routerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPppoeOffline(routerId)
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
}