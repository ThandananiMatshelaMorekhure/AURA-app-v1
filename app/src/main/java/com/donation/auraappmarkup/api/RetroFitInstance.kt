package com.donation.auraappmarkup.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.donation.auraappmarkup.Util.Constants.Companion.BASE_URL


class RetroFitInstance {
    companion object {
//        private const val BASE_URL = "https://newsapi.org/"

        private val retrofit: Retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api: ArticleAPI by lazy {
            retrofit.create(ArticleAPI::class.java)
        }
    }
}
