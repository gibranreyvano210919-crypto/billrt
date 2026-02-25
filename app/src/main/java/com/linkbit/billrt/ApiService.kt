package com.linkbit.billrt

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.linkbit.billrt.model.StandardResponse
import com.linkbit.billrt.network.PelangganBaruResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.Serializable

// region Data Classes

// Pengaturan
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

// Pelanggan
data class GetPelangganByIdRequest(val id_pelanggan: String)

data class GetPelangganByIdResponse(
    val status: Boolean,
    val message: String?,
    val data: PelangganData?
)

// Rekap
data class RekapStatusPelanggan(
    val aktif: Int,
    val isolir: Int,
    val nonaktif: Int,
    val total: Int,
    val lunas: Int,
    @SerializedName("belum_bayar") val belumBayar: Int,
    val tagout: Int,
    val telat: Int,
    @SerializedName("total_nunggak") val totalNunggak: Int
)

data class RekapStatusPelangganResponse(
    val status: Boolean,
    val message: String,
    val data: RekapStatusPelanggan
)

// Tagout
data class TagoutItem(
    @SerializedName("id_tagihan") val idTagihan: String,
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("nama_pelanggan") val namaPelanggan: String,
    @SerializedName("bulan_tagihan") val bulanTagihan: Int,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int,
    @SerializedName("status_tagihan") val statusTagihan: String,
    @SerializedName("catatan_tagout") val catatanTagout: String?,
    @SerializedName("updated_at") val updatedAt: String
) : Serializable

data class TagoutResponse(
    val status: Boolean,
    val message: String,
    val total: Int,
    val data: List<TagoutItem>
)

// Periode
data class PeriodeTagihan(
    @SerializedName("bulan_tagihan") val bulanTagihan: Int,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int
)

data class ListPeriodeResponse(
    val status: Boolean,
    val message: String?,
    val data: List<PeriodeTagihan>?
)

// Detail Tagihan
data class DetailBayarResponse(
    val status: Boolean,
    @SerializedName("is_lunas") val isLunas: Boolean,
    val message: String,
    val data: JsonElement
)

data class DetailBayarLunasData(
    @SerializedName("no_invoice") val noInvoice: String,
    @SerializedName("pelanggan") val pelanggan: String,
    @SerializedName("username") val username: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("wilayah") val wilayah: String,
    @SerializedName("tgl_instalasi") val tglInstalasi: String,
    @SerializedName("periode") val periode: String,
    @SerializedName("nominal") val nominal: Float,
    @SerializedName("tgl_bayar") val tglBayar: String,
    @SerializedName("metode") val metode: String,
    @SerializedName("admin_penerima") val adminPenerima: String,
    @SerializedName("catatan") val catatan: String
)

data class DetailBayarBelumLunasData(
    @SerializedName("no_invoice") val noInvoice: String,
    @SerializedName("pelanggan") val pelanggan: String,
    @SerializedName("username") val username: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("wilayah") val wilayah: String,
    @SerializedName("tgl_instalasi") val tglInstalasi: String,
    @SerializedName("periode") val periode: String,
    @SerializedName("nominal") val nominal: Float,
    @SerializedName("jatuh_tempo") val jatuhTempo: String,
    @SerializedName("instruksi") val instruksi: String
)

// endregion

interface ApiService {

    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // region Pengaturan
    @GET("index.php?tabel=pengaturan")
    fun getPengaturan(): Call<PengaturanResponse>

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
    // endregion

    // region Pelanggan
    @GET("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=data_pelanggan")
    fun getDataPelanggan(
        @Query("search") search: String? = null,
        @Query("filter_tipe") filterTipe: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("id_pelanggan") idPelanggan: String? = null,
        @Query("status_aktif") status: String? = null
    ): Call<PelangganResponse>

    @POST("index.php?tabel=get_pelanggan_by_id")
    fun getPelangganById(@Body request: GetPelangganByIdRequest): Call<GetPelangganByIdResponse>

    @GET("http://112.78.170.196:8890/billrt/api/api_pelanggan.php?tabel=apidetailpelanggan")
    fun getApiDetailPelanggan(@Query("id_pelanggan") idPelanggan: String): Call<ApiDetailPelangganResponse>

    @GET("index.php?tabel=pelanggan_baru")
    fun getPelangganBaru(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Call<PelangganBaruResponse>
    
    @GET("index.php?tabel=pelanggan_nonaktif")
    fun getPelangganNonaktif(
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?
    ): Call<PelangganNonaktifResponse>

    @GET("index.php?tabel=pelanggan_isolir")
    fun getPelangganIsolir(
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?
    ): Call<PelangganIsolirResponse>

    @POST("index.php?tabel=update_status_aktif")
    fun updateStatusPelanggan(@Body request: UpdateStatusRequest): Call<StandardResponse>

    @POST("http://112.78.170.196:8890/billrt/api/api_pelanggan.php?tabel=update_mac_pelanggan")
    fun updateMacAddress(@Query("id_pelanggan") idPelanggan: String, @Body request: UpdateMacRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_pelanggan")
    fun hapusPelanggan(@Body request: PelangganIdRequest): Call<StandardResponse>

    @POST("index.php?tabel=simpan_pelanggan")
    fun simpanPelanggan(@Body request: SimpanPelangganRequest): Call<StandardResponse>

    @POST("index.php?tabel=tambah_pelanggan")
    fun tambahPelanggan(@Body request: SimpanPelangganRequest): Call<StandardResponse>

    @POST("index.php")
    fun updatePelanggan(@Query("tabel") tabel: String, @Body request: UpdatePelangganRequest): Call<StandardResponse>
    // endregion

    // region Maps & ODP
    @GET("api_maps.php")
    fun getPelangganMap(@Query("q") q: String? = null): Call<GetPelangganResponse>

    @POST("index.php?tabel=update_koordinat_map")
    fun updateLokasi(@Body request: UpdateLokasiRequest): Call<StandardResponse>

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
    // endregion

    // region Master Data (Paket, Wilayah)
    @GET("index.php?tabel=paket_internet")
    fun getPaket(): Call<PaketResponse>

    @POST("index.php?tabel=simpan_paket")
    fun simpanPaket(@Body request: SimpanPaketRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_paket")
    fun hapusPaket(@Body request: HapusPaketRequest): Call<StandardResponse>

    @GET("index.php?tabel=wilayah")
    fun getWilayah(): Call<WilayahResponse>

    @GET("index.php?tabel=wilayah_pelanggan_nested")
    fun getWilayahPelangganNested(): Call<WilayahPelangganNestedResponse>

    @POST("index.php?tabel=simpan_wilayah")
    fun simpanWilayah(@Body request: SimpanWilayahRequest): Call<StandardResponse>

    @POST("index.php?tabel=hapus_wilayah")
    fun hapusWilayah(@Body request: HapusWilayahRequest): Call<StandardResponse>
    // endregion

    // region Laporan & Tagihan
    @GET("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=rekap_status_pelanggan")
    fun getRekapStatusPelanggan(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<RekapStatusPelangganResponse>

    @GET("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=get_tagout")
    fun getTagout(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Call<TagoutResponse>

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
    
    // Endpoint baru untuk detail pembayaran
    @GET("api_tagihan.php?tabel=detail_bayar_v2")
    suspend fun getDetailBayar(@Query("invoice") invoiceId: String): DetailBayarResponse
    
    @FormUrlEncoded
    @POST("api_tagihan.php?tabel=confirm_payment")
    suspend fun confirmPayment(
        @Field("id_tagihan") id_tagihan: String,
        @Field("metode_bayar") metode_bayar: String,
        @Field("tanggal_bayar") tanggal_bayar: String,
        @Field("keterangan") keterangan: String,
        @Field("admin_id") admin_id: Int
    ): StandardResponse
    // endregion

    // region Mikrotik & OLT
    @GET("index.php?tabel=master_teknisi")
    fun getMasterTeknisi(): Call<MasterTeknisiResponse>

    @GET("apimikrotik.php?tabel=mikrotik_accounts")
    fun getMikrotikAccounts(): Call<MikrotikAccountsResponse>

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
    // endregion
}
