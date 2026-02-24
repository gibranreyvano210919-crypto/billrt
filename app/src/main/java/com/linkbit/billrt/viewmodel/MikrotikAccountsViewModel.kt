package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.MikrotikAccount
import kotlinx.coroutines.launch

class MikrotikAccountsViewModel : ViewModel() {

    private val _accounts = MutableLiveData<List<MikrotikAccount>>()
    val accounts: LiveData<List<MikrotikAccount>> = _accounts

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchAccounts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMikrotikAccounts()
                if (response.isSuccessful && response.body()?.status == true) {
                    _accounts.postValue(response.body()?.data ?: emptyList())
                    _errorMessage.postValue(null)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengambil data akun"
                    _errorMessage.postValue(errorMsg)
                    _accounts.postValue(emptyList())
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan: ${e.message}")
                _accounts.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
