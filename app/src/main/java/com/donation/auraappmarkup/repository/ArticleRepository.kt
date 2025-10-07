package com.donation.auraappmarkup.repository

import com.donation.auraappmarkup.api.RetroFitInstance
import com.donation.auraappmarkup.db.ArticleDatabase
import com.donation.auraappmarkup.models.Article

class ArticleRepository(val db: ArticleDatabase) {
    // ✅ Keep your original API methods (used by ViewModel)
    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetroFitInstance.api.getHeadline(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetroFitInstance.api.searchForNews(searchQuery, pageNumber)

    // ✅ Keep your original database methods (used by ViewModel)
    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getFavouriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

    // ✅ ADD the new user preferences methods from generated code
    suspend fun getUserPrefs(userId: String) = db.getUserPrefsDao().getPreferences(userId)

    suspend fun saveUserPrefs(prefs: com.donation.auraappmarkup.db.UserPreferences) =
        db.getUserPrefsDao().insertPreferences(prefs)
}