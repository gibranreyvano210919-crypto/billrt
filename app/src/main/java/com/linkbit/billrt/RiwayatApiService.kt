package com.linkbit.billrt

import retrofit2.Call
import retrofit2.http.GET

interface RiwayatApiService {
    // This is a dedicated function for the new flat-list RiwayatKasFragment
    @GET("index.php?tabel=catatan_tagihan")
    fun getRiwayatKasList(): Call<RiwayatKasListResponse>
}