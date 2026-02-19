package com.linkbit.billrt.api

import com.linkbit.billrt.model.PembayaranHariIniResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("billrt/api/api_tagihan.php")
    fun getPembayaranHariIni(@Query("tabel") tabel: String = "list_pembayaran_hari_ini"): Call<PembayaranHariIniResponse>
}
