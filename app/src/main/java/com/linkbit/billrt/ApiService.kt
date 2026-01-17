package com.linkbit.billrt

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// 1. Model Data

//region Auth
data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val status: Boolean,
    val message: String,
    val data: UserData?
)

data class UserData(
    @SerializedName("id_user") val idUser: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("nama_lengkap") val nama: String? = null,
    @SerializedName("level") val level: String? = null
) : Serializable
//endregion

//region Pelanggan
data class TambahPelangganRequest(
    val nama_pelanggan: String?,
    val alamat_pelanggan: String?,
    val telepon_pelanggan: String?,
    val id_paket: Int?,
    val id_wilayah: Int?,
    val installation_date: String?,
    val mikrotik_username: String?,
    val mikrotik_password: String?,
    val latitude: Double?,
    val longitude: Double?,
    val mac_address: String?
)

data class PelangganResponse(
    val status: Boolean,
    val message: String,
    val rekap: RekapData?,
    val data: List<PelangganData>?
)

data class RekapData(
    @SerializedName("total_pelanggan") val totalPelanggan: Int = 0,
    @SerializedName("pelanggan_baru") val pelangganBaru: Int = 0,
    @SerializedName("pelanggan_lama") val pelangganLama: Int = 0,
    @SerializedName("total_lunas") val totalLunas: Int = 0,
    @SerializedName("total_belum_bayar") val totalBelumBayar: Int = 0,
    @SerializedName("total_pendapatan") val totalPendapatan: Float = 0f
)

data class PelangganData(
    @SerializedName("id_pelanggan") val idPelanggan: String? = null,
    @SerializedName("nama_pelanggan") val namaPelanggan: String? = null,
    @SerializedName("alamat_pelanggan") val alamat: String? = null,
    @SerializedName("telepon_pelanggan") val telepon: String? = null,
    @SerializedName("tgl_daftar") val tglDaftar: String? = null,
    @SerializedName("installation_date") val installationDate: String? = null,
    @SerializedName("nama_wilayah") val namaWilayah: String? = null,
    @SerializedName("mikrotik_username") val mikrotikUsername: String? = null,
    @SerializedName("static_ip") val staticIp: String? = null,
    @SerializedName("status_pppoe") val statusPppoe: String? = null,
    @SerializedName("mac_address") val macAddress: String? = null,
    @SerializedName("signal_rx") val signalRx: String? = null,
    @SerializedName("signal_tx") val signalTx: String? = null,
    @SerializedName("ftth_status") val ftthStatus: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("nama_paket") val namaPaket: String? = null,
    @SerializedName("harga_paket") val harga: String? = null,
    @SerializedName("id_paket") val idPaket: String? = null,
    val latitude: String? = null,
    val longitude: String? = null
) : Serializable
//endregion

//region Wilayah
data class WilayahResponse(
    val status: Boolean,
    @SerializedName("total_wilayah") val totalWilayah: Int = 0, 
    val data: List<Wilayah>?
)

data class Wilayah(
    @SerializedName("id_wilayah") val idWilayah: Int,
    @SerializedName("nama_wilayah") val namaWilayah: String,
    @SerializedName("jumlah_pelanggan") val jumlahPelanggan: Int
)

data class WilayahListResponse(
    val status: Boolean,
    val message: String,
    val data: List<WilayahListItem>?
)

data class WilayahListItem(
    @SerializedName("id_wilayah") val idWilayah: Int,
    @SerializedName("nama_wilayah") val namaWilayah: String
)
//endregion

//region Paket
data class PaketResponse(
    val status: Boolean,
    val data: List<Paket>?
)

data class Paket(
    @SerializedName("id_paket") val idPaket: Int,
    @SerializedName("nama_paket") val namaPaket: String,
    val harga: Float
)
//endregion

//region Keuangan
data class InputTagihanRequest(
    val id_pelanggan: Int,
    val bulan: Int,
    val tahun: Int,
    val total_bayar: Float,
    val id_paket: Int,
    val status_tagihan: String,
    val tgl_bayar: String?,
    val keterangan: String,
    val metode_bayar: String
)

data class StandardResponse(
    val status: Boolean,
    val message: String,
    @SerializedName("id_tagihan", alternate = ["id"]) val id: Int? = null
)

data class TagihanData(
    @SerializedName("id_tagihan2") var idTagihan2: String? = null,
    @SerializedName("id_pelanggan") val idPelanggan: String? = null,
    @SerializedName("id_paket") val idPaket: String? = null,
    @SerializedName("bulan_tagihan") val bulanTagihan: String? = null,
    @SerializedName("tahun_tagihan") val tahunTagihan: String? = null,
    @SerializedName("total_bayar") val totalBayar: String? = null,
    @SerializedName("status_tagihan") var statusTagihan: String? = null,
    @SerializedName("tgl_bayar") var tglBayar: String? = null
) : Serializable

data class BulanTagihanResponse(
    val status: Boolean,
    val message: String,
    val data: List<BulanTagihanData>?
)

data class BulanTagihanData(
    val angka: Int,
    val nama: String,
    @SerializedName("status_lunas") val statusLunas: Boolean,
    @SerializedName("total_bayar") val totalBayar: Float,
    @SerializedName("tanggal_bayar") val tanggalBayar: String?,
    @SerializedName("status_tagihan") val statusTagihan: String?
)

data class PembayaranResponse(
    val status: Boolean,
    val message: String,
    val data: List<PembayaranData>?
)

data class PembayaranData(
    @SerializedName("id_pembayaran") val idPembayaran: String? = null,
    @SerializedName("id_pelanggan") val idPelanggan: String? = null,
    @SerializedName("id_tagihan2") val idTagihan2: String? = null,
    @SerializedName("nama_pelanggan") val namaPelanggan: String? = null,
    @SerializedName("alamat_pelanggan") val alamatPelanggan: String? = null,
    @SerializedName("tgl_bayar") val tglBayar: String? = null,
    @SerializedName("metode_bayar") val metodeBayar: String? = null,
    @SerializedName("jumlah_bayar") val jumlahBayar: Float = 0f
) : Serializable

data class DeleteBody(val id: String)

data class RevertTagihanBody(
    val id_tagihan2: String,
    val status_tagihan: String,
    val tgl_bayar: String
)
//endregion

// 2. Interface API
interface ApiService {
    @POST("index.php?tabel=login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("index.php?tabel=tambah_pelanggan")
    fun tambahPelanggan(@Body request: TambahPelangganRequest): Call<StandardResponse>

    @GET("index.php")
    fun getUsers(@Query("tabel") tabel: String): Call<ResponseBody>

    @GET("index.php?tabel=data_pelanggan")
    fun getPelanggan( 
        @Query("bulan") bulan: Int?,
        @Query("tahun") tahun: Int?,
        @Query("filter_tipe") filterTipe: String = "semua",
        @Query("search") search: String? = null,
        @Query("id_wilayah") idWilayah: Int? = null
    ): Call<PelangganResponse>

    @GET("index.php?tabel=wilayah")
    fun getWilayahRekap(): Call<WilayahResponse>

    @GET("index.php?tabel=list_wilayah_saja")
    fun getWilayahList(): Call<WilayahListResponse>

    @GET("index.php?tabel=paket_internet")
    fun getPaket(): Call<PaketResponse>

    @GET("index.php?tabel=tagihan2")
    fun getTagihanBulanan(
        @Query("id_pelanggan") idPelanggan: Int,
        @Query("tahun") tahun: Int
    ): Call<BulanTagihanResponse>

    @GET("index.php?tabel=pembayaran2")
    fun getRiwayatPembayaran(@Query("limit") limit: Int = 20): Call<PembayaranResponse>

    @POST("index.php?tabel=input_tagihan2")
    fun createTagihan(@Body request: InputTagihanRequest): Call<StandardResponse>

    @POST("index.php?tabel=pembayaran2")
    fun createPembayaran(@Body pembayaran: PembayaranData): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "index.php?tabel=pembayaran2", hasBody = true)
    fun deletePembayaran(@Body body: DeleteBody): Call<ResponseBody>

    @PUT("index.php?tabel=tagihan2")
    fun revertTagihanStatus(@Body body: RevertTagihanBody): Call<ResponseBody>
}

// 3. Konfigurasi Retrofit
object ApiConfig {
    fun getApiService(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer d65c2eff72bd91186e3081f22b4db952904dab5373c85b0b1bca4d2e4c6207be")
                    .build()
                chain.proceed(request)
            }
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.78.170.196:8885/billrt/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
