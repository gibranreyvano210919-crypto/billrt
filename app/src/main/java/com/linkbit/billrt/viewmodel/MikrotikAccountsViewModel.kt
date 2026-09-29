package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.MikrotikAccount
import com.linkbit.billrt.model.StandardResponse
import kotlinx.coroutines.launch

class MikrotikAccountsViewModel : ViewModel() {

    private val _accounts = MutableLiveData<List<MikrotikAccount>>()
    val accounts: LiveData<List<MikrotikAccount>> = _accounts

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _defaultRouterId = MutableLiveData<Int>()
    val defaultRouterId: LiveData<Int> = _defaultRouterId

    private val _editStatus = MutableLiveData<StandardResponse?>()
    val editStatus: LiveData<StandardResponse?> = _editStatus

    private val _accountDetail = MutableLiveData<MikrotikAccount?>()
    val accountDetail: LiveData<MikrotikAccount?> = _accountDetail

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

    fun fetchAccountDetail(idRouter: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMikrotikAccountDetail(idRouter)
                if (response.isSuccessful && response.body()?.status == true) {
                    _accountDetail.postValue(response.body()?.data)
                } else {
                    _errorMessage.postValue("Gagal mengambil detail router")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Kesalahan: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addAccount(
        routerName: String,
        ipAddress: String,
        username: String,
        password: String,
        port: Int,
        ownerId: Int
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.addMikrotikAccount(
                    routerName, ipAddress, username, password, port, ownerId
                )
                if (response.isSuccessful) {
                    _editStatus.postValue(response.body())
                    if (response.body()?.status == true) {
                        fetchAccounts() // Refresh list
                    }
                } else {
                    _editStatus.postValue(StandardResponse(false, "Gagal menambahkan router"))
                }
            } catch (e: Exception) {
                _editStatus.postValue(StandardResponse(false, "Kesalahan: ${e.message}"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateAccount(
        id: Int,
        routerName: String,
        ipAddress: String,
        username: String,
        password: String?,
        port: Int,
        ownerId: Int
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.editMikrotikAccount(
                    id, routerName, ipAddress, username, password, port, ownerId
                )
                if (response.isSuccessful) {
                    _editStatus.postValue(response.body())
                    if (response.body()?.status == true) {
                        fetchAccounts() // Refresh list
                    }
                } else {
                    _editStatus.postValue(StandardResponse(false, "Gagal memperbarui data"))
                }
            } catch (e: Exception) {
                _editStatus.postValue(StandardResponse(false, "Kesalahan: ${e.message}"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteAccount(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteMikrotikAccount(id)
                if (response.isSuccessful) {
                    _editStatus.postValue(response.body())
                    if (response.body()?.status == true) {
                        fetchAccounts()
                    }
                } else {
                    _editStatus.postValue(StandardResponse(false, "Gagal menghapus router"))
                }
            } catch (e: Exception) {
                _editStatus.postValue(StandardResponse(false, "Kesalahan: ${e.message}"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun resetEditStatus() {
        _editStatus.value = null
    }

    fun clearAccountDetail() {
        _accountDetail.value = null
    }

    fun setAsDefault(idRouter: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.setSemuaRouter(idRouter)
                if (response.isSuccessful && response.body()?.status == true) {
                    _defaultRouterId.postValue(idRouter)
                    _editStatus.postValue(response.body())
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengatur router default"
                    _errorMessage.postValue(errorMsg)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Kesalahan: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun setDefaultRouterId(id: Int) {
        _defaultRouterId.value = id
    }
}
