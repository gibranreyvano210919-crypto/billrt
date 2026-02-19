package com.linkbit.billrt.network

import com.linkbit.billrt.model.BayarTagihanRequest
import com.linkbit.billrt.model.CustomerResponse
import com.linkbit.billrt.model.GenerateTagihanResponse
import com.linkbit.billrt.model.HistoryPembayaranApiResponse
import com.linkbit.billrt.model.ListPeriodeResponse
import com.linkbit.billrt.model.PelangganBelumBayarResponse
import com.linkbit.billrt.model.PelangganDetailResponse
import com.linkbit.billrt.model.PelangganLunasResponse
import com.linkbit.billrt.model.RekapJumlahPelangganResponse
import com.linkbit.billrt.model.RekapTagihanResponse
import com.linkbit.billrt.model.StandardResponse
import com.linkbit.billrt.model.TambahPembayaranRequest
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TagihanApiService {

    @GET("api_tagihan.php?tabel=get_list_periode")
    suspend fun getListPeriode(): ListPeriodeResponse

    @GET("api_tagihan.php?tabel=rekap_lunas_belum")
    suspend fun getRekapTagihan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): RekapTagihanResponse

    @GET("api_tagihan.php?tabel=rekap_jumlah_pelanggan")
    suspend fun getRekapJumlahPelanggan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): RekapJumlahPelangganResponse

    @GET("api_tagihan.php?tabel=pelanggan_belum_bayar")
    suspend fun getPelangganBelumBayar(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("id_wilayah") idWilayah: Int? = null,
        @Query("search") search: String? = null
    ): PelangganBelumBayarResponse

    @GET("api_tagihan.php?tabel=pelanggan_lunas")
    suspend fun getPelangganLunas(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
        @Query("search") search: String? = null
    ): PelangganLunasResponse

    @GET("api_tagihan.php?tabel=get_data_pelanggan_new")
    suspend fun getPelangganBaru(@Query("search") search: String?): CustomerResponse

    @POST("api_tagihan.php")
    suspend fun bayarTagihan(@Body request: BayarTagihanRequest): StandardResponse

    @POST("api_tagihan.php?tabel=tambah_pembayaran")
    suspend fun tambahPembayaran(@Body request: TambahPembayaranRequest): StandardResponse

    @FormUrlEncoded
    @POST("api_tagihan.php?tabel=batal_pembayaran")
    suspend fun batalPembayaran(@Field("id_tagihan") idTagihan: String): StandardResponse

    @GET("api_tagihan.php?tabel=generate_invoice")
    suspend fun generateTagihan(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): GenerateTagihanResponse

    @GET("api_tagihan.php?tabel=detail_pelanggan_bayar2")
    suspend fun getDetailPelangganBayar(@Query("id_pelanggan") idPelanggan: Int): HistoryPembayaranApiResponse

    @GET("api_tagihan.php?tabel=get_detail_pelanggan_new")
    suspend fun getDetailPelangganNew(@Query("id") id: Int): PelangganDetailResponse
}
