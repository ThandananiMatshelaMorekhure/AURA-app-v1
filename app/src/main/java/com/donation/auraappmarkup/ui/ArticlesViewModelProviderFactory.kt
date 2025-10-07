package com.donation.auraappmarkup.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.donation.auraappmarkup.repository.ArticleRepository

class ArticlesViewModelProviderFactory(val app: Application, val articleRepository: ArticleRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return ArticlesViewModel(app, articleRepository) as T
    }

}