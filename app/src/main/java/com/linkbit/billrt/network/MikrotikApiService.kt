package com.linkbit.billrt.network

import com.linkbit.billrt.model.HistoriCatatResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MikrotikApiService {

    @GET("index.php")
    fun getHistoriCatat(
        @Query("tabel") tabel: String = "riwayat_catat_pelanggan"
    ): Call<HistoriCatatResponse>
}
