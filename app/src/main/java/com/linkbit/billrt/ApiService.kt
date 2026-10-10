package com.linkbit.billrt

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.linkbit.billrt.model.*
import com.linkbit.billrt.network.PelangganBaruResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.Serializable

// region Data Classes

// Bill Statement
data class BillStatementSummary(
    @SerializedName("total_income") val totalIncome: Double,
    @SerializedName("income_format") val incomeFormat: String,
    @SerializedName("total_expense") val totalExpense: Double,
    @SerializedName("expense_format") val expenseFormat: String
) : Serializable

data class BillStatementFilter(
    val tipe: String,
    val bulan: Int,
    val tahun: Int
) : Serializable

data class BillStatementResponse(
    val status: Boolean,
    val filter: BillStatementFilter,
    val type: String,
    val summary: BillStatementSummary
) : Serializable

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
) : Serializable

data class UpdatePengaturanResponse(
    val status: Boolean,
    val message: String,
    val data: PengaturanResponse
) : Serializable

// Pelanggan
data class GetPelangganByIdRequest(val id_pelanggan: String) : Serializable

data class GetPelangganByIdResponse(
    val status: Boolean,
    val message: String?,
    val data: PelangganData?
) : Serializable

// Rekap
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
) : Serializable

// Periode
data class PeriodeTagihan(
    @SerializedName("bulan_tagihan") val bulanTagihan: Int,
    @SerializedName("tahun_tagihan") val tahunTagihan: Int
) : Serializable

data class ListPeriodeResponse(
    val status: Boolean,
    val message: String?,
    val data: List<PeriodeTagihan>?
) : Serializable

// Detail Tagihan
data class DetailBayarResponse(
    val status: Boolean,
    @SerializedName("is_lunas") val isLunas: Boolean,
    val message: String,
    val data: JsonElement
) : Serializable

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
) : Serializable

data class DetailBayarBelumLunasData(
    @SerializedName("no_invoice") val noInvoice: String,
    @SerializedName("pelanggan") val pelanggan: String,
    @SerializedName("username") val username: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("wilayah") val wilayah: String,
    @SerializedName("tgl_instalasi") val tglInstalasi: String,
    @SerializedName("periode") val periode: String,
    @SerializedName("nominal") val nominal: Float,
    @SerializedName("jatuh_tempo", alternate = ["tanggal_isolasi", "tgl_isolasi", "tgl_jatuh_tempo"]) val jatuhTempo: String,
    @SerializedName("instruksi") val instruksi: String
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
    @SerializedName("nama_pencatat") val namaPencatat: String?,
    @SerializedName("performa_pembayaran") val performaPembayaran: String?,
    @SerializedName("performa_float") val performaFloat: Float?,
    @SerializedName("invoice") val listIdTagihan: String?,
    @SerializedName("rincian_tunggakan") val rincianTunggakan: List<String>?
) : Serializable

data class TagihanBelumBayarResponse(
    val status: Boolean,
    val data: List<TagihanBelumBayar>
) : Serializable

data class TambahPembayaranMultiRequest(
    @SerializedName("id_tagihan_list") val idTagihanList: List<String>,
    @SerializedName("id_pelanggan") val idPelanggan: String,
    @SerializedName("metode_bayar") val metodeBayar: String,
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("tanggal_bayar") val tanggalBayar: String? = null
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
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("filter_tipe") filterTipe: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("id_pelanggan") idPelanggan: String? = null,
        @Query("status_aktif") status: String? = null
    ): Call<PelangganResponse>

    @GET("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=data_pelanggan_cepat")
    fun getDataPelangganCepat(
        @Query("search") search: String? = null,
        @Query("id_wilayah") idWilayah: Int? = null
    ): Call<PelangganResponse>

    @GET("api_tagihan.php?tabel=get_pelanggan_by_id")
    fun getPelangganById(@Query("id_pelanggan") id: String): Call<GetPelangganByIdResponse>

    @GET("api_pelanggan.php?tabel=apidetailpelanggan")
    fun getApiDetailPelanggan(@Query("id_pelanggan") idPelanggan: String): Call<ApiDetailPelangganResponse>

    @GET("api_pelanggan.php")
    fun getPelangganDetailV2(
        @Query("tabel") tabel: String = "apidetailpelanggan",
        @Query("id_pelanggan") idPelanggan: String
    ): Call<PelangganDetailV2Response>

    @GET("api_tagihan.php?tabel=pelanggan_baru")
    fun getPelangganBaru(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Call<PelangganBaruResponse>
    
    @GET("api_tagihan.php?tabel=pelanggan_nonaktif")
    fun getPelangganNonaktif(
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?
    ): Call<PelangganNonaktifResponse>

    @GET("api_tagihan.php?tabel=pelanggan_isolir")
    fun getPelangganIsolir(
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?
    ): Call<PelangganIsolirResponse>

    @POST("http://112.78.170.196:8885/billrt/api/index.php?tabel=update_status_aktif")
    fun updateStatusPelanggan(@Body request: UpdateStatusRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("api_pelanggan.php?tabel=update_mac_address")
    fun updateMacAddress(
        @Query("id_pelanggan") id_pelanggan: String,
        @Body request: UpdateMacRequest
    ): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=hapus_pelanggan")
    fun hapusPelanggan(@Body request: PelangganIdRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=simpan_pelanggan")
    fun simpanPelanggan(@Body request: SimpanPelangganRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=tambah_pelanggan")
    fun tambahPelanggan(@Body request: SimpanPelangganRequest): Call<TambahPelangganResponse>

    @POST("api_tagihan.php")
    fun updatePelanggan(@Query("tabel") tabel: String, @Body request: UpdatePelangganRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("api_pelanggan.php")
    fun updateTeleponPelanggan(
        @Query("tabel") tabel: String = "update_telepon_pelanggan",
        @Query("id_pelanggan") id_pelanggan: String,
        @Body request: UpdateTeleponRequest
    ): Call<com.linkbit.billrt.model.StandardResponse>

    @FormUrlEncoded
    @POST("api_tagihan.php?tabel=isolir_pelanggan")
    fun isolirPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<com.linkbit.billrt.model.StandardResponse>

    @FormUrlEncoded
    @POST("api_tagihan.php?tabel=nonaktif_pelanggan")
    fun nonaktifPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<com.linkbit.billrt.model.StandardResponse>

    @FormUrlEncoded
    @POST("api_tagihan.php?tabel=aktifkan_pelanggan")
    fun aktifkanPelanggan(
        @Field("id_pelanggan") idPelanggan: String
    ): Call<com.linkbit.billrt.model.StandardResponse>
    // endregion

    // region Maps & ODP
    @GET("api_maps.php")
    fun getPelangganMap(@Query("q") q: String? = null): Call<GetPelangganResponse>

    @POST("http://112.78.170.196:8885/billrt/api/index.php?tabel=update_koordinat_map")
    fun updateLokasi(@Body request: UpdateLokasiRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @GET("index.php?tabel=detail_pelanggan_map")
    fun getPelangganDetailMap(@Query("id_pelanggan") idPelanggan: String): Call<PelangganDetailMapResponse>

    @GET("index.php?tabel=odp")
    fun getOdp(@Query("search") search: String? = null): Call<OdpResponse>

    @GET("index.php?tabel=odp_ports")
    fun getOdpDetail(@Query("odpId") odpId: Int): Call<OdpDetailResponse>

    @GET("index.php?tabel=odp_lokasi_pelanggan")
    fun getOdpLokasiPelanggan(@Query("search") search: String? = null): Call<OdpLokasiPelangganResponse>

    @POST("index.php?tabel=simpan_odp")
    fun simpanOdp(@Body request: OdpData): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=update_port_odp")
    fun updatePortOdp(@Body request: OdpPort): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=hapus_odp")
    fun hapus_odp(@Body request: OdpData): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=simpan_jalur_kabel")
    fun simpanJalurKabel(@Body request: SimpanJalurKabelRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=hapus_jalur_kabel")
    fun hapusJalurKabel(@Body request: HapusJalurKabelRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=cabut_layanan_port")
    fun cabutLayananPort(@Body request: CabutLayananPortRequest): Call<com.linkbit.billrt.model.StandardResponse>
    // endregion

    // region Master Data (Paket, Wilayah)
    @GET("api_tagihan.php?tabel=paket_internet")
    fun getPaket(): Call<PaketResponse>

    @POST("index.php?tabel=simpan_paket")
    fun simpanPaket(@Body request: SimpanPaketRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=hapus_paket")
    fun hapusPaket(@Body request: HapusPaketRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @GET("api_tagihan.php?tabel=wilayah")
    fun getWilayah(): Call<WilayahResponse>

    @GET("http://112.78.170.196:8885/billrt/api/index.php?tabel=wilayah_pelanggan_nested")
    fun getWilayahPelangganNested(@Query("cari") cari: String? = null): Call<WilayahPelangganNestedResponse>

    @POST("index.php?tabel=simpan_wilayah")
    fun simpanWilayah(@Body request: SimpanWilayahRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=hapus_wilayah")
    fun hapusWilayah(@Body request: HapusWilayahRequest): Call<com.linkbit.billrt.model.StandardResponse>
    // endregion

    // region Laporan & Tagihan
    @GET("api_tagihan.php?tabel=rekap_status_pelanggan")
    fun getRekapStatusPelanggan(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<RekapStatusPelangganResponse>

    @GET("api_tagihan.php?tabel=get_tagout")
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
    fun getRiwayatCatatan(
        @Query("id_pelanggan") idPelanggan: String,
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<RiwayatCatatanResponse>

    @GET("http://112.78.170.196:8885/billrt/api/index.php?tabel=catatan_tagihan")
    fun getCatatanTagihan(
        @Query("id_teknisi") idTeknisi: String,
        @Query("search") search: String,
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<GroupedKasResponse>

    @POST("index.php?tabel=tambah_catatan")
    fun tambahCatatan(@Query("bulan") bulan: Int, @Query("tahun") tahun: Int, @Body request: InputCatatanRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=hapus_catatan")
    fun hapusCatatan(@Body request: HapusCatatanRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=verify_catatan")
    fun verifyCatatan(@Body request: VerifyCatatanRequest): Call<com.linkbit.billrt.model.StandardResponse>

    @POST("index.php?tabel=unverify_catatan")
    fun unverifyCatatan(@Body request: UnverifyCatatanRequest): Call<com.linkbit.billrt.model.StandardResponse>
    
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
    ): com.linkbit.billrt.model.StandardResponse

    @GET("api_tagihan.php?tabel=pelanggan_belum_bayar_all")
    fun getPelangganBelumBayarAll(
        @Query("id_pelanggan") id_pelanggan: String
    ): Call<TagihanBelumBayarResponse>

    @POST("http://112.78.170.196:8890/billrt/api/api_tagihan.php?tabel=tambah_pembayaran_multi")
    fun tambahPembayaranMulti(@Body request: TambahPembayaranMultiRequest): Call<NotaDataResponse>

    @GET("api_tagihan.php?tabel=list_pembayaran_pencarian")
    fun getListPembayaranPencarian(
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("search") search: String? = null
    ): Call<PembayaranHariIniResponse>

    @GET("api_tagihan.php?tabel=list_pembayaran_hari_ini")
    fun getPembayaranHariIni(
        @Query("tanggal") tanggal: String? = null,
        @Query("search") search: String? = null,
        @Query("id_user_pencatat") idUser: Int? = null
    ): Call<PembayaranHariIniResponse>

    @GET("api_tagihan.php?tabel=rekap_total_income_expense")
    fun getBillStatement(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Call<BillStatementResponse>

    @GET("api_tagihan.php?tabel=detail_nunggak_tahunan")
    fun getDetailNunggakTahunan(@Query("tahun") tahun: Int): Call<NunggakTahunanResponse>

    @GET("api_pembukuan.php?tabel=pemasukan")
    fun getPemasukan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("id_user_pencatat") idUserpencatat: Int,
        @Query("search") search: String
    ): Call<PemasukanResponse>

    @GET("api_tagihan.php?tabel=list_admin")
    fun getListAdmin(): Call<AdminResponse>
    // endregion

    // region Mikrotik & OLT
    @GET("index.php?tabel=master_teknisi")
    fun getMasterTeknisi(): Call<MasterTeknisiResponse>

    @GET("api_mikrotik.php?tabel=list_mikrotik")
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
    ): Call<com.linkbit.billrt.model.StandardResponse>

    @GET("smartolt.php")
    fun getOltData(): Call<OltApiResponse>

    @GET("smartolt_highspeed.php")
    fun getHighSpeedOltData(): Call<HighSpeedOltResponse>

    @GET("api_master_olt.php?tabel=master_olt")
    fun getMasterOlt(): Call<OltResponse>

    @GET("api_master_olt.php?tabel=detail_master_olt")
    fun getOltDetail(@Query("id_olt") id_olt: Int): Call<OltDetailResponse>
    // endregion
}
