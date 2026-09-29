package com.linkbit.billrt.network

import com.google.gson.annotations.SerializedName
import com.linkbit.billrt.DetailBayarResponse
import com.linkbit.billrt.PelangganListResponse
import com.linkbit.billrt.PelangganResponse
import com.linkbit.billrt.WilayahPelangganNestedResponse
import com.linkbit.billrt.model.*
import com.linkbit.billrt.UpdateTeleponRequest
import com.linkbit.billrt.UpdateMacRequest
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("billrt/api/api_radius.php")
    fun searchAutocomplete(@Query("search") searchQuery: String): Call<SearchAutoCompleteResponse>

    @GET("billrt/api/api_radius.php")
    fun searchRadiusById(@Query("id_pelanggan") idPelanggan: String): Call<RadiusByIdResponse>

    @GET("billrt/api/index.php?tabel=pelanggan_tanpa_lokasi")
    fun getPelangganTanpaLokasi(): Call<PelangganTanpaLokasiResponse>

    @POST("billrt/api/index.php?tabel=ganti_lokasi")
    fun saveLocation(@Body request: SaveLocationRequest): Call<SaveLocationResponse>

    @GET("billrt/api/index.php?tabel=semua_pelanggan_lokasi")
    fun getSemuaPelanggan(): Call<PelangganListResponse>

    @GET("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=data_pelanggan")
    fun getDataPelanggan(
        @Query("search") search: String? = null,
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("filter_tipe") filterTipe: String? = "semua",
        @Query("status_aktif") statusAktif: String? = "semua",
        @Query("id_pelanggan") idPelanggan: String? = null
    ): Call<DashboardResponse>

    @GET("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=data_pelanggan_cepat")
    fun getDataPelangganCepat(
        @Query("search") search: String? = null,
        @Query("id_wilayah") idWilayah: Int? = null
    ): Call<PelangganResponse>

    @GET("billrt/api/api_pelanggan.php")
    fun getPelangganDetailV2(
        @Query("tabel") tabel: String = "apidetailpelanggan",
        @Query("id_pelanggan") idPelanggan: String
    ): Call<PelangganDetailV2Response>

    @GET("billrt/api/index.php?tabel=pelanggan_baru")
    fun getPelangganBaru(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Call<PelangganBaruResponse>

    @GET("billrt/api/index.php?tabel=data_pelanggan_list")
    fun getDataPelangganList(@Query("search") search: String? = null): Call<PelangganListResponse>

    @GET("billrt/api/index.php?tabel=wilayah_pelanggan_nested")
    fun getWilayahPelangganNested(): Call<WilayahPelangganNestedResponse>

    @GET("billrt/api/index.php?tabel=master_wilayah")
    suspend fun getWilayah(): WilayahResponse

    @GET("billrt/api/index.php?tabel=master_teknisi_list")
    suspend fun getTeknisi(): TeknisiResponse

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=isolir_pelanggan")
    fun isolirPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<StandardResponse>

    @FormUrlEncoded
    @POST("billrt/api/api_tagihan.php?tabel=nonaktif_pelanggan")
    fun nonaktifPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<StandardResponse>

    @POST("billrt/api/api_pelanggan.php")
    fun updateTeleponPelanggan(
        @Query("tabel") tabel: String = "update_telepon_pelanggan",
        @Query("id_pelanggan") idPelanggan: String,
        @Body request: UpdateTeleponRequest
    ): Call<StandardResponse>

    @POST("billrt/api/api_pelanggan.php")
    fun updateMacAddress(
        @Query("id_pelanggan") idPelanggan: String,
        @Body request: UpdateMacRequest,
        @Query("tabel") tabel: String = "update_mac_pelanggan"
    ): Call<StandardResponse>

    @GET("billrt/api/apimikrotik.php?tabel=mikrotik_accounts")
    fun getMikrotikAccounts(): Call<MikrotikAccountsResponse>

    @GET("billrt/api/apimikrotik.php?tabel=mikrotik_account_get")
    fun getMikrotikAccountDetail(
        @Query("id_router") idRouter: Int
    ): Call<MikrotikAccountDetailResponse>

    @FormUrlEncoded
    @POST("billrt/api/apimikrotik.php?tabel=mikrotik_account_edit")
    fun editMikrotikAccount(
        @Field("id") id: Int,
        @Field("router_name") routerName: String,
        @Field("ip_address") ipAddress: String,
        @Field("username") username: String,
        @Field("password") password: String?,
        @Field("port") port: Int,
        @Field("owner_id") ownerId: Int
    ): Call<StandardResponse>

    @POST("billrt/api/index.php?tabel=tambah_catatan")
    fun tambahCatatan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Body request: InputCatatanRequest
    ): Call<StandardResponse>

    @GET("billrt/api/index.php?tabel=master_teknisi")
    fun getMasterTeknisi(): Call<MasterTeknisiResponse>

    @GET("billrt/api/index.php?tabel=catatan_tagihan")
    fun getCatatanTagihan(
        @Query("id_teknisi") idTeknisi: String,
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<CatatanTagihanResponse>

    @GET("billrt/api/index.php?tabel=riwayat_catat_pelanggan")
    fun getHistoriCatat(
        @Query("id_pelanggan") idPelanggan: String? = null,
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("cek_bulan") cekBulan: Int? = null,
        @Query("cek_tahun") cekTahun: Int? = null
    ): Call<HistoriCatatResponse>

    @GET("billrt/api/index.php?tabel=rekap_tunggakan_lanjutan")
    suspend fun getRekapTunggakanLanjutan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("id_wilayah") idWilayah: Int? = 0,
        @Query("id_teknisi") idTeknisi: Int? = 0
    ): RekapTunggakanLanjutanResponse

    @GET("billrt/api/index.php?tabel=riwayat_setoran")
    fun getRiwayatSetoran(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("id_teknisi") idTeknisi: String? = null
    ): Call<RiwayatSetoranResponse>

    @POST("billrt/api/index.php?tabel=tambah_setoran")
    fun tambahSetoran(@Body request: TambahSetoranRequest): Call<StandardResponse>

    @POST("billrt/api/index.php?tabel=edit_catatan_setoran")
    fun editCatatanSetoran(@Body request: EditCatatanSetoranRequest): Call<StandardResponse>

    @POST("billrt/api/index.php?tabel=hapus_setoran")
    fun hapusSetoran(@Body request: HapusSetoranRequest): Call<StandardResponse>

    @GET("billrt/api/api_tagihan.php?tabel=detail_bayar_v2")
    suspend fun getDetailBayar(@Query("invoice") invoiceId: String): DetailBayarResponse
}

data class RekapTunggakanLanjutanResponse(
    val status: Boolean,
    val total: Int,
    @SerializedName("periode_target")
    val periodeTarget: String,
    val data: List<RekapTunggakanLanjutan>
)

data class WilayahResponse(
    val status: Boolean,
    val data: List<Wilayah>
)

data class TeknisiResponse(
    val status: Boolean,
    val data: List<Teknisi>
)
