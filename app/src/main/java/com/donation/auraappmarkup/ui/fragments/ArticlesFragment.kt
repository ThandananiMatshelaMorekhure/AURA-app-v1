package com.donation.auraappmarkup.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.donation.auraappmarkup.R
import com.donation.auraappmarkup.databinding.FragmentArticlesBinding
import com.donation.auraappmarkup.ui.Articles
import com.donation.auraappmarkup.ui.ArticlesViewModel
import com.google.android.material.snackbar.Snackbar

class ArticlesFragment : Fragment(R.layout.fragment_articles) {

    private lateinit var articlesViewModel: ArticlesViewModel
    private val args: ArticlesFragmentArgs by navArgs()
    private lateinit var binding: FragmentArticlesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticlesBinding.bind(view)

        articlesViewModel = (activity as Articles).articlesViewModel
        val article = args.article

        binding.webView.apply {
            webViewClient = WebViewClient()
            article.url?.let { url ->
                loadUrl(url)
            }
        }

        binding.fab.setOnClickListener {
            articlesViewModel.addToFavourite(article)
            Snackbar.make(view, "Added to favourites", Snackbar.LENGTH_SHORT).show()
        }
    }
}