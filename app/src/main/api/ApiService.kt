package com.linkbit.billrt.api

import com.google.gson.annotations.SerializedName
import com.linkbit.billrt.LoginRequest
import com.linkbit.billrt.LoginResponse
import com.linkbit.billrt.model.* 
import com.linkbit.billrt.HighSpeedOltResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.Serializable

// region Models

data class PelangganNonaktif(
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("nama_wilayah") val namaWilayah: String?,
    @SerializedName("status_aktif") val statusAktif: String?,
    @SerializedName("tgl_nonaktif") val tglNonaktif: String?
) : Serializable

data class PelangganNonaktifResponse(
    val status: Boolean,
    val data: List<PelangganNonaktif>
) : Serializable

data class BillStatementSummary(
    @SerializedName("total_income") val totalIncome: Double,
    @SerializedName("income_format") val incomeFormat: String,
    @SerializedName("total_expense") val totalExpense: Double,
    @SerializedName("expense_format") val expenseFormat: String
) : Serializable

data class BillStatementResponse(
    val status: Boolean,
    val summary: BillStatementSummary
) : Serializable

// Model untuk endpoint: grafik_pembayaran
data class GrafikPembayaranItem(
    val x: String, // Format: "YYYY-MM-DD"
    val y: Float   // Total nominal
) : Serializable

data class GrafikPembayaranSummary(
    @SerializedName("total_nominal_bulan_ini") val totalNominalBulanIni: Float,
    @SerializedName("jumlah_hari_terisi") val jumlahHariTerisi: Int
) : Serializable

data class GrafikPembayaranResponse(
    val status: Boolean,
    val message: String,
    val summary: GrafikPembayaranSummary,
    val data: List<GrafikPembayaranItem>
) : Serializable

// Model untuk endpoint: listlunas
data class ListLunasItem(
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    val periode: String,
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    val jumlah: Double,
    val metode: String?,
    @SerializedName("id_user_pencatat") val idUserPencatat: Int
) : Serializable

data class ListLunasSummary(
    @SerializedName("total_lunas") val totalLunas: Int,
    @SerializedName("total_nominal") val totalNominal: Double
) : Serializable

data class ListLunasResponse(
    val status: Boolean,
    val summary: ListLunasSummary,
    val data: List<ListLunasItem>
) : Serializable

data class RekapListLunasResponse(
    val summary: ListLunasSummary
) : Serializable

// endregion

interface ApiService {

    @POST("billrt/api/login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

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
    fun getPembayaranHariIni(
        @Query("tanggal") tanggal: String? = null,
        @Query("search") search: String? = null,
        @Query("id_user_pencatat") idUser: Int? = null
    ): Call<PembayaranHariIniResponse>

    @GET("billrt/api/api_tagihan.php?tabel=list_pembayaran_pencarian")
    fun getListPembayaranPencarian(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("search") search: String? = null
    ): Call<PembayaranHariIniResponse>

    @GET("billrt/api/api_tagihan.php?tabel=transaksi_hari_ini")
    fun getTransaksiHariIni(
        @Query("tanggal") tanggal: String,
        @Query("id_user_pencatat") adminId: Int? = null,
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("search") search: String? = null
    ): Call<TransaksiResponse>

    @GET("billrt/api/api_tagihan.php?tabel=list_admin")
    fun getListAdmin(): Call<AdminResponse>

    @GET("smartolt_highspeed.php")
    fun getHighSpeedOltData(): Call<HighSpeedOltResponse>

    @GET("billrt/api/api_tagihan.php?tabel=rekap_total_income_expense")
    fun getBillStatement(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<BillStatementResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran")
    fun getGrafikPembayaran(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<GrafikPembayaranResponse>

    @GET("billrt/api/api_tagihan.php?tabel=listlunastanggal")
    fun getListLunas(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("search") search: String? = null
    ): Call<ListLunasResponse>

    @GET("billrt/api/api_tagihan.php?tabel=rekaplistlunas")
    fun getRekapListLunas(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<RekapListLunasResponse>

    @GET("billrt/api/api_tagihan.php?tabel=pelanggan_nonaktif")
    fun getPelangganNonaktif(
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?
    ): Call<PelangganNonaktifResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=aktifkan_pelanggan")
    fun aktifkanPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<StandardResponse>
}
