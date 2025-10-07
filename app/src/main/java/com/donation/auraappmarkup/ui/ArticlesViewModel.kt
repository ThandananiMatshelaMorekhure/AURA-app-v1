package com.donation.auraappmarkup.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.donation.auraappmarkup.Util.Resource
import com.donation.auraappmarkup.models.Article
import com.donation.auraappmarkup.models.ArticleResponse
import com.donation.auraappmarkup.repository.ArticleRepository
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class ArticlesViewModel(
    app: Application,
    val articleRepository: ArticleRepository
) : AndroidViewModel(app) {

    // LiveData for headlines
    val headlines: MutableLiveData<Resource<ArticleResponse>> = MutableLiveData()
    var headlinesPage = 1
    private var headlinesResponse: ArticleResponse? = null

    // LiveData for search
    val searchNews: MutableLiveData<Resource<ArticleResponse>> = MutableLiveData()
    var searchNewsPage = 1
    private var searchArticleResponse: ArticleResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    // ------------------ HANDLE RESPONSES ------------------ //

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: Response<ArticleResponse>): Resource<ArticleResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticles = headlinesResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<ArticleResponse>): Resource<ArticleResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchArticleResponse == null || newSearchQuery != oldSearchQuery) {
                    searchArticleResponse = resultResponse
                } else {
                    val oldArticles = searchArticleResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchArticleResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    // ------------------ DATABASE OPS ------------------ //

    fun addToFavourite(article: Article) = viewModelScope.launch {
        articleRepository.upsert(article)
    }

    fun getFavouriteNews() = articleRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        articleRepository.deleteArticle(article)
    }

    // ------------------ USER PREFERENCES OPS ------------------ //

    fun saveUserPreferences(prefs: com.donation.auraappmarkup.db.UserPreferences) = viewModelScope.launch {
        articleRepository.saveUserPrefs(prefs)
    }

    suspend fun getUserPreferences(userId: String): com.donation.auraappmarkup.db.UserPreferences? {
        return articleRepository.getUserPrefs(userId)
    }

    // ------------------ NETWORK CHECK ------------------ //

    fun internetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    // ------------------ API CALLS ------------------ //

    fun headlinesInternet(countryCode: String) = viewModelScope.launch {
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(getApplication())) {
                val response = articleRepository.getHeadlines(countryCode, headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Network Failure"))
                else -> headlines.postValue(
                    Resource.Error(t.message ?: "An unexpected error occurred")
                )
            }
        }
    }

    fun searchNewsInternet(searchQuery: String) = viewModelScope.launch {
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())

        try {
            if (internetConnection(getApplication())) {
                // reset pagination if query changed
                if (newSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    searchArticleResponse = null
                    oldSearchQuery = newSearchQuery
                }

                val response = articleRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))

            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(
                    Resource.Error(t.message ?: "An unexpected error occurred")
                )
            }
        }
    }
}