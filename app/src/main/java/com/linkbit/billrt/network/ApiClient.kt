package com.linkbit.billrt.network

import com.linkbit.billrt.ApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://112.78.170.196:8885/billrt/api/"

    val instance: ApiService by lazy {
        // Interceptor untuk logging, sangat berguna untuk debug
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Konfigurasi OkHttpClient untuk menonaktifkan cache
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cache(null) // Eksplisit menonaktifkan cache
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Gunakan OkHttpClient yang sudah dikonfigurasi
            .build()
        retrofit.create(ApiService::class.java)
    }
}
