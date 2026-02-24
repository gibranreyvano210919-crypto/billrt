package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.PppoeSecret
import kotlinx.coroutines.launch

class DaftarSecretViewModel : ViewModel() {

    private val _secrets = MutableLiveData<List<PppoeSecret>>()
    val secrets: LiveData<List<PppoeSecret>> = _secrets

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun fetchSecrets(routerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPppoeSecret(routerId)
                if (response.isSuccessful && response.body()?.status == true) {
                    _secrets.postValue(response.body()?.data ?: emptyList())
                } else {
                    _errorMessage.postValue(response.body()?.message ?: "Gagal memuat data secret")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun setSecretStatus(routerId: Int, secret: PppoeSecret, isEnabled: Boolean) {
        val newStatus = if (isEnabled) "enable_secret" else "disable_secret"
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.setSecretStatus(newStatus, routerId, secret.id)
                if (response.isSuccessful && response.body()?.status == true) {
                    _toastMessage.postValue("Status ${secret.name} berhasil diubah")
                    // Refresh list after status change
                    val currentList = _secrets.value?.toMutableList() ?: mutableListOf()
                    val index = currentList.indexOfFirst { it.id == secret.id }
                    if (index != -1) {
                        currentList[index] = secret.copy(disabled = (!isEnabled).toString())
                        _secrets.postValue(currentList)
                    }
                } else {
                    _toastMessage.postValue(response.body()?.message ?: "Gagal mengubah status")
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
