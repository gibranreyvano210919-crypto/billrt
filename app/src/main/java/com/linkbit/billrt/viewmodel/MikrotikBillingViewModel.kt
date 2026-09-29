package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.MikrotikBillingItem
import com.linkbit.billrt.model.StandardResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MikrotikBillingViewModel : ViewModel() {

    private val _billingItems = MutableLiveData<List<MikrotikBillingItem>>()
    val billingItems: LiveData<List<MikrotikBillingItem>> = _billingItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun fetchBillingData(routerId: Int, search: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMikrotikBilling(routerId, search)
                if (response.isSuccessful && response.body()?.status == true) {
                    _billingItems.postValue(response.body()?.data ?: emptyList())
                    _errorMessage.postValue(null)
                } else {
                    val msg = response.body()?.message ?: "Gagal memuat data billing"
                    _errorMessage.postValue(msg)
                    _billingItems.postValue(emptyList())
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Kesalahan: ${e.message}")
                _billingItems.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun hapusPelanggan(idPelanggan: String, routerId: Int, search: String?) {
        _isLoading.value = true
        RetrofitInstance.api.hapusPelanggan(idPelanggan).enqueue(object : Callback<StandardResponse> {
            override fun onResponse(
                call: Call<StandardResponse>,
                response: Response<StandardResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == true) {
                    _successMessage.postValue(response.body()?.message ?: "Pelanggan berhasil dihapus")
                    fetchBillingData(routerId, search)
                } else {
                    _errorMessage.postValue(response.body()?.message ?: "Gagal menghapus pelanggan")
                    _isLoading.postValue(false)
                }
            }

            override fun onFailure(call: Call<StandardResponse>, t: Throwable) {
                _errorMessage.postValue("Error: ${t.message}")
                _isLoading.postValue(false)
            }
        })
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
