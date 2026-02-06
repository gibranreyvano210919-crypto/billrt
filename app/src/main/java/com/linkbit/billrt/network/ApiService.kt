package com.linkbit.billrt.network

import com.linkbit.billrt.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interface Retrofit terpusat untuk semua endpoint aplikasi.
 */
interface ApiService {

    // region Pencarian & Pelanggan Umum
    @GET("billrt/api/api_radius.php")
    fun searchAutocomplete(@Query("search") searchQuery: String): Call<SearchAutoCompleteResponse>

    @GET("billrt/api/api_radius.php")
    fun searchRadiusById(@Query("id_pelanggan") idPelanggan: String): Call<RadiusByIdResponse>

    @GET("billrt/api/index.php?tabel=pelanggan_tanpa_lokasi")
    fun getPelangganTanpaLokasi(): Call<PelangganTanpaLokasiResponse>

    @POST("billrt/api/index.php?tabel=ganti_lokasi")
    fun saveLocation(@Body request: SaveLocationRequest): Call<SaveLocationResponse>

    @GET("billrt/api/index.php?tabel=semua_pelanggan_lokasi")
    fun getSemuaPelanggan(): Call<SemuaPelangganResponse>

    @GET("billrt/api/index.php?tabel=data_pelanggan")
    fun getDataPelanggan(
        @Query("search") search: String? = null,
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("filter_tipe") filterTipe: String? = "semua",
        @Query("status_aktif") statusAktif: String? = "semua"
    ): Call<DashboardResponse>
    // endregion

    // region Fitur MikroTik
    @GET("billrt/api/apimikrotik.php?tabel=mikrotik_accounts")
    fun getMikrotikAccounts(): Call<MikrotikAccountsResponse>

    @GET("billrt/api/index.php?tabel=status_pelanggan")
    fun getPelangganStatus(@Query("id") routerId: Int): Call<PelangganStatusResponse>

    @GET("billrt/api/index.php?tabel=audit_user")
    fun getAuditUser(@Query("id") routerId: Int): Call<AuditUserResponse>
    // endregion

    // region Catatan & Kas
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
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<CatatanTagihanResponse>
    // endregion

    // region Setoran
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
    // endregion
}