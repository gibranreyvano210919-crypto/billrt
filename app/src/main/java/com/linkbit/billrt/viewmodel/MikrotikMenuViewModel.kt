package com.linkbit.billrt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkbit.billrt.api.RetrofitInstance
import com.linkbit.billrt.model.MikrotikDashboardResponse
import kotlinx.coroutines.launch

// region Audit Models
data class AuditGlobalResponse(
    val status: Boolean,
    val data: List<AuditRouterItem>
)

data class AuditRouterItem(
    val router: String,
    @com.google.gson.annotations.SerializedName("hilang_di_router") val hilangDiRouter: AuditDetail,
    @com.google.gson.annotations.SerializedName("user_gelap") val userGelap: AuditDetail
)

data class AuditDetail(
    val total: Int,
    val list: List<String>
)
// endregion

class MikrotikMenuViewModel : ViewModel() {

    private val _dashboardData = MutableLiveData<MikrotikDashboardResponse?>()
    val dashboardData: LiveData<MikrotikDashboardResponse?> = _dashboardData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _auditResult = MutableLiveData<AuditGlobalResponse?>()
    val auditResult: LiveData<AuditGlobalResponse?> = _auditResult

    fun fetchDashboardData(routerId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMikrotikDashboard(routerId)
                if (response.isSuccessful && response.body()?.status == true) {
                    _dashboardData.postValue(response.body())
                    _errorMessage.postValue(null)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal memuat dashboard. Kode: ${response.code()}"
                    _errorMessage.postValue(errorMsg)
                    _dashboardData.postValue(null)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                _dashboardData.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun auditUserGlobal() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.auditUserGlobal()
                if (response.isSuccessful && response.body() != null) {
                    _auditResult.postValue(response.body())
                } else {
                    _errorMessage.postValue("Gagal melakukan audit pppoe")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error audit: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
