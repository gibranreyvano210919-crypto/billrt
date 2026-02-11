package com.linkbit.billrt

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data class for the response of the settings endpoint
data class PengaturanResponse(
    @SerializedName("id_pengaturan") val idPengaturan: String?,
    @SerializedName("nama_perusahaan") val namaPerusahaan: String?,
    @SerializedName("alamat_perusahaan") val alamatPerusahaan: String?,
    @SerializedName("telepon_perusahaan") val teleponPerusahaan: String?,
    @SerializedName("email_perusahaan") val emailPerusahaan: String?,
    @SerializedName("bank_nama") val bankNama: String?,
    @SerializedName("bank_atas_nama") val bankAtasNama: String?,
    @SerializedName("bank_no_rekening") val bankNoRekening: String?,
    @SerializedName("logo_perusahaan") val logoPerusahaan: String?,
    @SerializedName("masa_tenggang") val masaTenggang: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class UpdatePengaturanResponse(
    val status: Boolean,
    val message: String,
    val data: PengaturanResponse
)

interface ApiService {

    @GET("index.php")
    fun getPengaturan(@Query("tabel") tabel: String = "pengaturan"): Call<PengaturanResponse>

    @FormUrlEncoded
    @POST("index.php?tabel=pengaturan")
    fun updatePengaturan(
        @Field("id_pengaturan") id: Int,
        @Field("nama_perusahaan") nama: String,
        @Field("alamat_perusahaan") alamat: String,
        @Field("telepon_perusahaan") telepon: String,
        @Field("email_perusahaan") email: String,
        @Field("bank_nama") bank: String,
        @Field("bank_atas_nama") an: String,
        @Field("bank_no_rekening") norek: String,
        @Field("masa_tenggang") tenggang: Int
    ): Call<UpdatePengaturanResponse>

    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("api_maps.php") // Tetap untuk mengambil semua data pelanggan di peta
    fun getPelangganMap(@Query("q") q: String? = null): Call<GetPelangganResponse>

    // Endpoint telah diperbarui sesuai dengan skrip PHP Anda
    @POST("index.php?tabel=update_koordinat_map")
    fun updateLokasi(@Body request: UpdateLokasiRequest): Call<StandardResponse>

    // Endpoint baru untuk mengambil detail pelanggan di peta
    @GET("index.php?tabel=detail_pelanggan_map")
    fun getPelangganDetailMap(@Query("id_pelanggan") idPelanggan: String): Call<PelangganDetailMapResponse>

    @GET("index.php?tabel=odp")
    fun getOdp(@Query("search") search: String? = null): Call<OdpResponse>

    @GET("index.php?tabel=odp_ports")
    fun getOdpDetail(@Query("odp_id") odpId: Int): Call<OdpDetailResponse>

    @GET("index.php?tabel=odp_lokasi_pelanggan")
    fun getOdpLokasiPelanggan(@Query("search") search: String? = null): Call<OdpLokasiPelangganResponse>

    @POST("index.php?tabel=simpan_odp")
    fun simpanOdp(@Body request: OdpData): Call<StandardResponse>

    @POST("index.php?tabel=update_port_odp")
    fun updatePortOdp(@Body request: OdpPort): Call<StandardResponse>

    @POST("index.php?tabel=hapus_odp")
    fun hapusOdp(@Body request: OdpData): Call<StandardResponse>

    @POST("index.php?tabel=simpan_jalur_kabel")
    fun simpanJalurKabel(@Body request: SimpanJalurKabelRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_jalur_kabel")
    fun hapusJalurKabel(@Body request: HapusJalurKabelRequest): Call<StandardResponse>

    @POST("index.php?tabel=cabut_layanan_port")
    fun cabutLayananPort(@Body request: CabutLayananPortRequest): Call<StandardResponse>
    
    @GET("index.php?tabel=wilayah_pelanggan_nested")
    fun getWilayahPelangganNested(): Call<WilayahPelangganNestedResponse>


    @GET("index.php?tabel=data_pelanggan")
    fun getDataPelanggan(
        @Query("search") search: String? = null,
        @Query("filter_tipe") filterTipe: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("id_pelanggan") idPelanggan: String? = null,
        @Query("status_aktif") status: String? = null // Added for filtering
    ): Call<PelangganResponse>

    @POST("index.php?tabel=update_status_aktif")
    fun updateStatusPelanggan(@Body request: UpdateStatusRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_pelanggan")
    fun hapusPelanggan(@Body request: PelangganIdRequest): Call<StandardResponse>

    @POST("index.php?tabel=simpan_pelanggan")
    fun simpanPelanggan(@Body request: SimpanPelangganRequest): Call<StandardResponse>

    @GET("index.php?tabel=paket_internet")
    fun getPaket(): Call<PaketResponse>

    @POST("index.php?tabel=simpan_paket")
    fun simpanPaket(@Body request: SimpanPaketRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_paket")
    fun hapusPaket(@Body request: HapusPaketRequest): Call<StandardResponse>

    @GET("index.php?tabel=wilayah")
    fun getWilayah(): Call<WilayahResponse>

    @POST("index.php?tabel=simpan_wilayah")
    fun simpanWilayah(@Body request: SimpanWilayahRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_wilayah")
    fun hapusWilayah(@Body request: HapusWilayahRequest): Call<StandardResponse>

    @GET("index.php?tabel=laporan_wilayah")
    fun getLaporanWilayah(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<List<DetailWilayah>>

    @GET("index.php?tabel=rekap_tercatat")
    fun getRekapTercatat(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<RekapTercatatResponse>

    @GET("index.php?tabel=rekap_tunggakan")
    fun getRekapTunggakan(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<RekapTunggakanResponse>

    @GET("index.php?tabel=riwayat_catatan")
    fun getRiwayatCatatan(@Query("id_pelanggan") idPelanggan: String): Call<RiwayatCatatanResponse>

    @GET("index.php?tabel=catatan_tagihan")
    fun getCatatanTagihan(@Query("search") search: String, @Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<GroupedKasResponse>

    @POST("index.php?tabel=tambah_catatan")
    fun tambahCatatan(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int, @Body request: InputCatatanRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_catatan")
    fun hapusCatatan(@Body request: HapusCatatanRequest): Call<StandardResponse>

    @POST("index.php?tabel=verify_catatan")
    fun verifyCatatan(@Body request: VerifyCatatanRequest): Call<StandardResponse>

    @POST("index.php?tabel=unverify_catatan")
    fun unverifyCatatan(@Body request: UnverifyCatatanRequest): Call<StandardResponse>

    @GET("index.php?tabel=master_teknisi")
    fun getMasterTeknisi(): Call<MasterTeknisiResponse>

    @GET("apimikrotik.php")
    fun getMikrotikAccounts(
        @Query("tabel") tabel: String = "mikrotik_accounts"
    ): Call<MikrotikAccountsResponse>

    @GET("apimikrotik.php")
    fun getPelangganStatus(
        @Query("tabel") tabel: String = "pelanggan_status",
        @Query("id") routerId: Int
    ): Call<PelangganStatusResponse>

    @FormUrlEncoded
    @POST("apimikrotik.php?tabel=remove_active")
    fun removeActive(
        @Field("id") id: Int,
        @Field("username") username: String
    ): Call<StandardResponse>

    @GET("apimikrotik.php")
    fun auditUser(
        @Query("tabel") tabel: String = "audit_user",
        @Query("id") routerId: Int
    ): Call<AuditUserResponse>

    @GET("smartolt.php")
    fun getOltData(): Call<OltApiResponse>

    @GET("get_pelanggan_detail.php")
    fun getPelangganDetail(@Query("id") id: Int): Call<PelangganDetailResponse>
}
