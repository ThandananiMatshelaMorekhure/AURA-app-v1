package com.donation.auraappmarkup.api

import com.donation.auraappmarkup.Util.Constants.Companion.API_Key
import com.donation.auraappmarkup.models.ArticleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticleAPI {


    @GET("v2/everything") //top-headlines
    suspend fun getHeadline(
        @Query("country")
        countryCode: String = "us",
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_Key
    ): Response<ArticleResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apikey")
        apiKey: String = API_Key
    ): Response<ArticleResponse>



}