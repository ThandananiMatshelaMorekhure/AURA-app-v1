package com.donation.auraappmarkup.models

data class ArticleResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)