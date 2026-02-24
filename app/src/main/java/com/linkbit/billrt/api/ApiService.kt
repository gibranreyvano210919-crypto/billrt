package com.linkbit.billrt.api

import com.linkbit.billrt.model.AvailableIpResponse
import com.linkbit.billrt.model.MikrotikAccountsResponse
import com.linkbit.billrt.model.MikrotikDashboardResponse
import com.linkbit.billrt.model.PembayaranHariIniResponse
import com.linkbit.billrt.model.PppoeOfflineResponse
import com.linkbit.billrt.model.PppoeOnlineResponse
import com.linkbit.billrt.model.PppoeProfileResponse
import com.linkbit.billrt.model.PppoeSecretResponse
import com.linkbit.billrt.model.StandardResponse
import com.linkbit.billrt.model.TransaksiResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("billrt/api/api_mikrotik.php?tabel=dashboard")
    suspend fun getMikrotikDashboard(@Query("router_id") routerId: Int): Response<MikrotikDashboardResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=mikrotik_account")
    suspend fun getMikrotikAccounts(): Response<MikrotikAccountsResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_offline")
    suspend fun getPppoeOffline(@Query("id") routerId: Int): Response<PppoeOfflineResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_online")
    suspend fun getPppoeOnline(@Query("id") routerId: Int): Response<PppoeOnlineResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=kick_user")
    suspend fun kickUser(@Query("router_id") routerId: Int, @Query("user_id") userId: String): Response<StandardResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_secret")
    suspend fun getPppoeSecret(@Query("id") routerId: Int): Response<PppoeSecretResponse>

    @GET("billrt/api/api_mikrotik.php")
    suspend fun setSecretStatus(
        @Query("tabel") tabel: String, // "enable_secret" or "disable_secret"
        @Query("router_id") routerId: Int,
        @Query("user_id") userId: String
    ): Response<StandardResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_profile")
    suspend fun getPppoeProfiles(@Query("id") routerId: Int): Response<PppoeProfileResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=get_available_ip")
    suspend fun getAvailableIps(@Query("id") routerId: Int, @Query("pool") pool: String): Response<AvailableIpResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_mikrotik.php?tabel=pppoe_add")
    suspend fun addPppoeSecret(
        @Query("router_id") routerId: Int,
        @Field("user") user: String,
        @Field("pass") pass: String,
        @Field("profile") profile: String,
        @Field("comment") comment: String?,
        @Field("local_ip") localIp: String?,
        @Field("remote_ip") remoteIp: String?
    ): Response<StandardResponse>

    @GET("billrt/api/api_tagihan.php?tabel=list_pembayaran_hari_ini")
    fun getPembayaranHariIni(): Call<PembayaranHariIniResponse>

    @GET("billrt/api/api_tagihan.php?tabel=pembayaran")
    fun getTransaksi(
        @Query("tgl_mulai") tglMulai: String,
        @Query("tgl_akhir") tglAkhir: String,
        @Query("id_admin") adminId: Int,
        @Query("search") search: String
    ): Call<TransaksiResponse>
}
