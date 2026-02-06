package com.linkbit.billrt

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RiwayatKasApiService {
    // Updated to accept filter parameters
    @GET("index.php")
    fun getRiwayatKasList(
        @Query("tabel") tabel: String,
        @Query("bulan") bulan: String?,
        @Query("tahun") tahun: String?,
        @Query("search") search: String?
    ): Call<RiwayatKasListResponse>
}