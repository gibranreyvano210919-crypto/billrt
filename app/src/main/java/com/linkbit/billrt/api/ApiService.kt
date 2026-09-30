package com.linkbit.billrt.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.linkbit.billrt.LoginRequest
import com.linkbit.billrt.LoginResponse
import com.linkbit.billrt.model.*
import com.linkbit.billrt.HighSpeedOltResponse
import com.linkbit.billrt.viewmodel.AuditGlobalResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.Serializable

// region Models
data class BillStatementSummary(
    @SerializedName("total_income") val totalIncome: Double,
    @SerializedName("income_format") val incomeFormat: String,
    @SerializedName("total_expense") val totalExpense: Double,
    @SerializedName("expense_format") val expenseFormat: String,
    @SerializedName("total_online") val totalOnline: Double = 0.0,
    @SerializedName("total_setoran") val totalSetoran: Double = 0.0,
    @SerializedName("total_selisih") val totalSelisih: Double = 0.0
) : Serializable

data class BillStatementResponse(
    val status: Boolean,
    val summary: BillStatementSummary
) : Serializable

data class GrafikPembayaranItem(
    val x: String, 
    val label: Int? = null,
    val y: Float? = null,   
    @SerializedName("y_nominal_tepat") val yNominalTepat: Float? = null,
    @SerializedName("y_nominal_telat") val yNominalTelat: Float? = null,
    @SerializedName("y_pelanggan_tepat") val yPelangganTepat: Int? = null,
    @SerializedName("y_pelanggan_telat") val yPelangganTelat: Int? = null,
    @SerializedName("y_pelanggan") val yPelanggan: Int? = null,
    @SerializedName("y_telat") val yTelat: Int? = null,
    @SerializedName("is_sunday") val isSunday: Int? = null
) : Serializable

data class UserDataPencatat(
    @SerializedName("nama_pencatat") val namaPencatat: String,
    @SerializedName("total_pelanggan") val totalPelanggan: Int,
    @SerializedName("total_nominal") val totalNominal: Double,
    @SerializedName("total_telat") val totalTelat: Int,
    @SerializedName("tepat_waktu") val tepatWaktu: Int
) : Serializable

data class GrafikPembayaranPencatatItem(
    val x: String,
    @SerializedName("users_data") val usersData: List<UserDataPencatat>
) : Serializable

data class GrafikPembayaranPencatatResponse(
    val status: Boolean,
    val message: String,
    val data: List<GrafikPembayaranPencatatItem>
) : Serializable

data class ListPencatatItem(
    @SerializedName("id_user_pencatat") val idUserPencatat: Int,
    @SerializedName("nama_pencatat") val namaPencatat: String,
    @SerializedName("total_transaksi") val totalTransaksi: Int,
    @SerializedName("total_pelanggan") val totalPelanggan: Int,
    @SerializedName("total_nominal") val totalNominal: Double
) : Serializable

data class ListPencatatFilter(
    val bulan: Int,
    val tahun: Int
) : Serializable

data class ListPencatatResponse(
    val status: Boolean,
    @SerializedName("filter_aktif") val filterAktif: ListPencatatFilter? = null,
    val data: List<ListPencatatItem>
) : Serializable

data class ListPencatatDetailItem(
    val name: String? = null,
    val nama: String,
    var wilayah: String? = null,
    @SerializedName("tanggal_bayar") val tanggalBayar: String,
    val nominal: Double,
    val metode: String,
    @SerializedName("periode_tagihan") val periodeTagihan: String,
    val indikator: String? = null
) : Serializable

data class ListPencatatDetailGroup(
    val wilayah: String,
    @SerializedName("total_nominal") val totalNominal: Double,
    @SerializedName("pelanggan_baru") val pelangganBaru: Int,
    @SerializedName("pelanggan_lama") val pelangganLama: Int,
    @SerializedName("detail_list") val detailList: List<ListPencatatDetailItem>
) : Serializable

data class ListPencatatDetailFilter(
    @SerializedName("id_user_pencatat") val idUserPencatat: Int,
    val periode: String
) : Serializable

data class ListPencatatDetailResponse(
    val status: Boolean,
    val filter: ListPencatatDetailFilter? = null,
    val message: String? = null,
    val data: List<ListPencatatDetailGroup>? = null
) : Serializable

data class GrafikPembayaranSummary(
    @SerializedName("total_nominal") val totalNominal: Double? = null,
    @SerializedName("total_pelanggan") val totalPelanggan: Int? = null,
    @SerializedName("total_uang_masuk") val totalUangMasuk: Double? = null,
    @SerializedName("total_pelanggan_bayar") val totalPelangganBayar: Int? = null,
    @SerializedName("jumlah_hari") val jumlahHari: Int? = null
) : Serializable

data class GrafikPembayaranResponse(
    val status: Boolean,
    val message: String,
    val summary: GrafikPembayaranSummary? = null,
    val data: List<GrafikPembayaranItem>
) : Serializable

data class ListLunasTanggalItem(
    val nama: String,
    val periode: String,
    @SerializedName("tanggal_bayar") val tanggalBayar: String,
    val nominal: Double,
    val metode: String,
    @SerializedName("status_periode") val statusPeriode: String,
    @SerializedName("nama_pencatat") val namaPencatat: String? = null
) : Serializable

data class GroupedLunasTanggal(
    val tanggal: String,
    @SerializedName("total_harian") val totalHarian: Double,
    val list: List<ListLunasTanggalItem>
) : Serializable

data class ListLunasTanggalResponse(
    val status: Boolean,
    val data: List<GroupedLunasTanggal>
) : Serializable

data class ListLunasItem(
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    val periode: String,
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    val jumlah: Double,
    val metode: String?,
    @SerializedName("id_user_pencatat") val idUserPencatat: Int
) : Serializable

data class ListLunasResponse(
    val status: Boolean,
    val filter_aktif: ListLunasFilterAktif? = null,
    val summary: ListLunasSummary? = null,
    val data: List<ListLunasItem>
) : Serializable

data class ListLunasFilterAktif(
    val bulan: Int,
    val tahun: Int
) : Serializable

data class ListLunasSummary(
    @SerializedName("total_lunas") val totalLunas: Int,
    @SerializedName("total_nominal") val totalNominal: Double
) : Serializable

data class RekapListLunasResponse(
    val summary: ListLunasSummary
) : Serializable

data class RekapStatusPelanggan(
    val aktif: String?,
    val isolir: Int,
    val nonaktif: Int,
    val total: Int,
    val lunas: Int,
    @SerializedName("belum_bayar") val belumBayar: Int,
    val tagout: Int,
    val telat: Int,
    @SerializedName("total_nunggak") val totalNunggak: Int,
    @SerializedName("nunggak_tahunan") val nunggakTahunan: Int,
    @SerializedName("telat_tahunan") val telatTahunan: Int,
    val baru: Int
) : Serializable

data class RekapStatusPelangganResponse(
    val status: Boolean,
    val message: String,
    val data: RekapStatusPelanggan
) : Serializable

data class TagihanBelumBayar(
    @SerializedName("id_pelanggan") val idPelanggan: Int,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("telepon_pelanggan") val teleponPelanggan: String?,
    @SerializedName("mikrotik_username") val mikrotikUsername: String?,
    @SerializedName("nama_paket") val namaPaket: String?,
    @SerializedName("harga_paket") val hargaPaket: Float,
    @SerializedName("wilayah") val wilayah: String?,
    @SerializedName("periode_tunggakan") val periodeTunggakan: String?,
    @SerializedName("total_nominal") val totalNominal: Float,
    @SerializedName("tgl_bayar_terakhir") val tglBayarTerakhir: String?,
    @SerializedName("invoice") val listIdTagihan: String?,
    @SerializedName("rincian_tunggakan") val rincianTunggakan: List<String>?
) : Serializable

data class TagihanBelumBayarResponse(
    val status: Boolean,
    val data: List<TagihanBelumBayar>
) : Serializable

data class DetailBayarResponse(
    val status: Boolean,
    @SerializedName("is_lunas") val isLunas: Boolean,
    val message: String,
    val data: JsonElement
) : Serializable

data class NunggakTahunanItem(
    val id: Int,
    val nama: String,
    val telepon: String?,
    val alamat: String?,
    val wilayah: String?,
    val paket: String?,
    val username: String?,
    @SerializedName("jumlah_bulan") val jumlahBulan: Int,
    @SerializedName("list_bulan") val listBulan: String?
) : Serializable

data class NunggakTahunanResponse(
    val status: Boolean,
    val message: String,
    val data: List<NunggakTahunanItem>
) : Serializable

data class AiQueryResponse(
    val status: Boolean,
    val mode: String?,
    @SerializedName("sql_generated") val sqlGenerated: String?,
    val data: List<Map<String, Any>>?,
    val message: String?
) : Serializable

data class ApiSettingItem(
    val id: Int? = null,
    @SerializedName("setting_key") val settingKey: String,
    @SerializedName("setting_value") val settingValue: String?
) : Serializable

data class ApiSettingsResponse(
    val status: Boolean,
    val message: String? = null,
    val data: List<ApiSettingItem>? = null
) : Serializable

data class AiModelItem(
    val id: Int? = null,
    @SerializedName("model_name") val modelName: String,
    val temperature: Double? = 0.10,
    @SerializedName("extra_params") val extraParams: String? = null,
    @SerializedName("is_active") val isActive: Int = 0
) : Serializable

data class AiModelsResponse(
    val status: Boolean,
    val message: String? = null,
    val data: List<AiModelItem>? = null
) : Serializable
// endregion

interface ApiService {

    @GET("http://112.78.170.196:8885/billrt/api/api_settings.php")
    fun getApiSettings(): Call<ApiSettingsResponse>

    @FormUrlEncoded
    @POST("http://112.78.170.196:8885/billrt/api/api_settings.php")
    fun saveApiSetting(
        @Field("id") id: Int? = null,
        @Field("setting_key") settingKey: String,
        @Field("setting_value") settingValue: String
    ): Call<StandardResponse>

    @FormUrlEncoded
    @POST("http://112.78.170.196:8885/billrt/api/api_settings.php")
    fun deleteApiSetting(
        @Field("id") id: Int,
        @Field("action") action: String = "delete"
    ): Call<StandardResponse>

    @GET("http://112.78.170.196:8885/billrt/api/api_settings.php?endpoint=models")
    fun getAiModels(): Call<AiModelsResponse>

    @FormUrlEncoded
    @POST("http://112.78.170.196:8885/billrt/api/api_settings.php?endpoint=models")
    fun saveAiModel(
        @Field("id") id: Int? = null,
        @Field("model_name") modelName: String,
        @Field("temperature") temperature: Double,
        @Field("extra_params") extraParams: String?,
        @Field("is_active") isActive: Int
    ): Call<StandardResponse>

    @FormUrlEncoded
    @POST("http://112.78.170.196:8885/billrt/api/api_settings.php?endpoint=models")
    fun deleteAiModel(
        @Field("id") id: Int,
        @Field("action") action: String = "delete"
    ): Call<StandardResponse>

    @GET("http://112.78.170.196:8885/billrt/api/api_mistral.php")
    fun getAiQuery(@Query("tanya_ai") pertanyaan: String): Call<AiQueryResponse>

    @POST("billrt/api/login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // region Bill Statement
    @GET("billrt/api/api_tagihan.php?tabel=rekap_total_income_expense")
    fun getBillStatement(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<BillStatementResponse>

    @GET("billrt/api/api_tagihan.php?tabel=listlunas")
    fun getListLunas(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int, @Query("search") search: String? = null): Call<ListLunasResponse>

    @GET("billrt/api/api_tagihan.php?tabel=list_pencatat")
    fun getListPencatat(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<ListPencatatResponse>

    @GET("billrt/api/api_tagihan.php?tabel=list_pencatat_detail")
    fun getListPencatatDetail(
        @Query("id_user_pencatat") idUser: Int,
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<ListPencatatDetailResponse>

    @GET("billrt/api/api_tagihan.php?tabel=listlunastanggal")
    fun getListLunasTanggal(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<ListLunasTanggalResponse>

    @GET("billrt/api/api_tagihan.php?tabel=rekaplistlunas")
    fun getRekapListLunas(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<RekapListLunasResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran")
    fun getGrafikPembayaran(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<GrafikPembayaranResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran_status")
    fun getGrafikPembayaranStatus(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<GrafikPembayaranResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran_tanggal")
    fun getGrafikPembayaranTanggal(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<GrafikPembayaranResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran_periode")
    fun getGrafikPembayaranPeriode(
        @Query("bulan_awal") bulanAwal: String,
        @Query("bulan_akhir") bulanAkhir: String
    ): Call<GrafikPembayaranResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran_pencatat")
    fun getGrafikPembayaranPencatat(
        @Query("bulan_awal") bulanAwal: String,
        @Query("bulan_akhir") bulanAkhir: String
    ): Call<GrafikPembayaranPencatatResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pembayaran_pencatat_harian")
    fun getGrafikPembayaranPencatatHarian(
        @Query("tanggal_awal") tanggalAwal: String,
        @Query("tanggal_akhir") tanggalAkhir: String
    ): Call<GrafikPembayaranPencatatResponse>

    @GET("billrt/api/api_tagihan.php?tabel=grafik_pencatat_harian_detail")
    fun getGrafikHarianDetail(
        @Query("id_user_pencatat") idUser: Int,
        @Query("bulan") bulan: String,
        @Query("tahun") tahun: Int
    ): Call<GrafikPembayaranResponse>
    // endregion

    // region Mikrotik
    @GET("billrt/api/api_mikrotik.php?tabel=dashboard")
    suspend fun getMikrotikDashboard(@Query("router_id") routerId: Int): Response<MikrotikDashboardResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=mikrotik_account")
    suspend fun getMikrotikAccounts(): Response<MikrotikAccountsResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=mikrotik_account_get")
    suspend fun getMikrotikAccountDetail(
        @Query("id") idRouter: Int
    ): Response<MikrotikAccountDetailResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=set_semua_router")
    suspend fun setSemuaRouter(
        @Field("id_router") idRouter: Int
    ): Response<StandardResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_mikrotik.php?tabel=mikrotik_account_add")
    suspend fun addMikrotikAccount(
        @Field("router_name") routerName: String,
        @Field("ip_address") ipAddress: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("port") port: Int,
        @Field("owner_id") ownerId: Int
    ): Response<StandardResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_mikrotik.php?tabel=mikrotik_account_edit")
    suspend fun editMikrotikAccount(
        @Field("id") id: Int,
        @Field("router_name") routerName: String,
        @Field("ip_address") ipAddress: String,
        @Field("username") username: String,
        @Field("password") password: String?,
        @Field("port") port: Int,
        @Field("owner_id") ownerId: Int
    ): Response<StandardResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_mikrotik.php?tabel=mikrotik_account_delete")
    suspend fun deleteMikrotikAccount(
        @Field("id") id: Int
    ): Response<StandardResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_offline")
    suspend fun getPppoeOffline(@Query("id") routerId: Int): Response<PppoeOfflineResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_online")
    suspend fun getPppoeOnline(@Query("id") routerId: Int): Response<PppoeOnlineResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=mikrotik_billing")
    suspend fun getMikrotikBilling(
        @Query("filter_router") routerId: Int,
        @Query("search") search: String? = null
    ): Response<MikrotikBillingResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_kick")
    suspend fun kickUser(@Query("router_id") routerId: Int, @Query("mikrotik_username") username: String): Response<com.linkbit.billrt.model.StandardResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=audit_user_global")
    suspend fun auditUserGlobal(): Response<AuditGlobalResponse>

    @GET("billrt/api/api_mikrotik.php?tabel=pppoe_secret")
    suspend fun getPppoeSecret(@Query("id") routerId: Int): Response<PppoeSecretResponse>

    @GET("billrt/api/api_mikrotik.php")
    suspend fun setSecretStatus(@Query("tabel") tabel: String, @Query("router_id") routerId: Int, @Query("user_id") userId: String): Response<com.linkbit.billrt.model.StandardResponse>

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
    ): Response<com.linkbit.billrt.model.StandardResponse>

    // Sync MikroTik Pelanggan
    @GET("billrt/api/api_sync_pelanggan.php")
    suspend fun getSyncPelangganList(
        @Query("action") action: String = "list_pelanggan",
        @Query("id_router") idRouter: Int? = null,
        @Query("id") routerId: Int? = null
    ): Response<SyncMikrotikListResponse>

    @GET("billrt/api/api_sync_pelanggan.php")
    suspend fun syncSinglePelanggan(
        @Query("action") action: String = "fetch_single",
        @Query("username") username: String,
        @Query("id_router") idRouter: Int? = null,
        @Query("id") routerId: Int? = null
    ): Response<SyncSingleResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_sync_pelanggan.php")
    suspend fun saveSinglePelanggan(
        @Query("action") action: String = "save_single",
        @Field("id_pelanggan") idPelanggan: String,
        @Field("mac_baru") macBaru: String,
        @Field("ip_val") ipVal: String
    ): Response<StandardResponse>

    @GET("billrt/api/api_sync_mikrotik.php")
    suspend fun getSyncMikrotikList(
        @Query("action") action: String = "get_list"
    ): Response<SyncMikrotikListResponse>

    @GET("billrt/api/api_sync_mikrotik.php")
    suspend fun syncSingleMikrotik(
        @Query("action") action: String = "sync_single",
        @Query("username") username: String
    ): Response<SyncSingleResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_sync_mikrotik.php")
    suspend fun saveSingleMikrotik(
        @Query("action") action: String = "save_single",
        @Field("id_pelanggan") idPelanggan: String,
        @Field("mac_baru") macBaru: String,
        @Field("ip_val") ipVal: String
    ): Response<StandardResponse>

    @GET("billrt/api/sync_mikrotik.php")
    suspend fun getSyncMikrotikListAlt(
        @Query("action") action: String = "get_list"
    ): Response<SyncMikrotikListResponse>

    @GET("billrt/api/sync_mikrotik.php")
    suspend fun syncSingleMikrotikAlt(
        @Query("action") action: String = "sync_single",
        @Query("username") username: String
    ): Response<SyncSingleResponse>

    @FormUrlEncoded
    @POST("billrt/api/sync_mikrotik.php")
    suspend fun saveSingleMikrotikAlt(
        @Query("action") action: String = "save_single",
        @Field("id_pelanggan") idPelanggan: String,
        @Field("mac_baru") macBaru: String,
        @Field("ip_val") ipVal: String
    ): Response<StandardResponse>
    // endregion

    // region Transaksi & Tagihan
    @GET("billrt/api/api_tagihan.php?tabel=list_pembayaran_hari_ini")
    fun getPembayaranHariIni(
        @Query("tanggal") tanggal: String? = null,
        @Query("search") search: String? = null,
        @Query("id_user_pencatat") idUser: Int? = null
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

    @GET("billrt/api/api_tagihan.php?tabel=rekap_status_pelanggan")
    fun getRekapStatusPelanggan(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<RekapStatusPelangganResponse>

    @GET("billrt/api/api_tagihan.php?tabel=pelanggan_belumbayar_all")
    fun getPelangganBelumBayarAll(@Query("id_pelanggan") id_pelanggan: String): Call<TagihanBelumBayarResponse>

    @GET("billrt/api/api_tagihan.php?tabel=detail_bayar_v2")
    suspend fun getDetailBayar(@Query("invoice") invoiceId: String): DetailBayarResponse

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=confirm_payment")
    suspend fun confirmPayment(
        @Field("id_tagihan") id_tagihan: String,
        @Field("metode_bayar") metode_bayar: String,
        @Field("tanggal_bayar") tanggal_bayar: String,
        @Field("keterangan") keterangan: String,
        @Field("admin_id") admin_id: Int
    ): com.linkbit.billrt.model.StandardResponse

    @GET("billrt/api/smartolt_highspeed.php")
    fun getHighSpeedOltData(): Call<HighSpeedOltResponse>

    @GET("billrt/api/api_tagihan.php?tabel=detail_nunggak_tahunan")
    fun getDetailNunggakTahunan(@Query("tahun") tahun: Int): Call<NunggakTahunanResponse>

    @GET("billrt/api/api_tagihan.php?tabel=pelanggan_baru")
    fun getPelangganBaru(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<com.linkbit.billrt.network.PelangganBaruResponse>

    @GET("billrt/api/api_tagihan.php?tabel=pelanggan_nonaktif")
    fun getPelangganNonaktif(
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?
    ): Call<com.linkbit.billrt.PelangganNonaktifResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=aktifkan_pelanggan")
    fun aktifkanPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<com.linkbit.billrt.model.StandardResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=hapus_pelanggan")
    fun hapusPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<com.linkbit.billrt.model.StandardResponse>
    // endregion

    // region Pembukuan
    @GET("billrt/api/api_pembukuan.php?tabel=pemasukan")
    fun getPemasukan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("id_user_pencatat") idUserPencatat: Int,
        @Query("search") search: String
    ): Call<PemasukanResponse>
    // endregion

    // region Pengeluaran
    @GET("billrt/api/api_pengeluaran.php?tabel=master_kategori")
    fun getMasterKategori(): Call<MasterKategoriResponse>

    @GET("billrt/api/api_pengeluaran.php?tabel=list_pengeluaran")
    fun getListPengeluaran(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<PengeluaranResponse>

    @POST("billrt/api/api_pengeluaran.php?tabel=tambah_pengeluaran")
    fun tambahPengeluaran(@Body body: Map<String, @JvmSuppressWildcards Any>): Call<com.linkbit.billrt.model.StandardResponse>

    @PUT("billrt/api/api_pengeluaran.php?tabel=edit_pengeluaran")
    fun editPengeluaran(@Body body: Map<String, @JvmSuppressWildcards Any>): Call<com.linkbit.billrt.model.StandardResponse>

    @DELETE("billrt/api/api_pengeluaran.php?tabel=hapus_pengeluaran")
    fun hapusPengeluaran(@Query("id_pengeluaran") id: Int): Call<com.linkbit.billrt.model.StandardResponse>
    // endregion
}
