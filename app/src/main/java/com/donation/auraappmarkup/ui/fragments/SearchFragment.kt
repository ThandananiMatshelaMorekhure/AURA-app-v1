package com.donation.auraappmarkup.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.donation.auraappmarkup.Adapter.ArticleAdapter
import com.donation.auraappmarkup.R
import com.donation.auraappmarkup.Util.Constants
import com.donation.auraappmarkup.Util.Resource
import com.donation.auraappmarkup.databinding.FragmentSearchBinding
import com.donation.auraappmarkup.ui.Articles
import com.donation.auraappmarkup.ui.ArticlesViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    lateinit var articlesViewModel: ArticlesViewModel
    lateinit var articleAdapter: ArticleAdapter
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)

        itemSearchError = view.findViewById(R.id.itemSearchError)
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        articlesViewModel = (activity as Articles).articlesViewModel
        setupSearchRecycler()

        articleAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("articles", it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articlesFragment, bundle)
        }
        var job: Job? = null
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                job?.cancel()
                job = MainScope().launch {
                    delay(Constants.SEARCH_NEWS_TIME_DELAY)
                    editable?.let {
                        if (editable.toString().isNotEmpty()) {
                            articlesViewModel.searchNews(editable.toString())
                        }
                    }
                }
            }
        })

        articlesViewModel.searchNews.observe(viewLifecycleOwner,Observer { response ->
                when (response) {
                    is Resource.Success<*> -> {
                        hideProgressBar()
                        hideErrorMessage()
                        response.data?.let { articleResponse ->
                            articleAdapter.differ.submitList(articleResponse.articles.toList())
                            val totalPages =
                                articleResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                            isLastPage = articlesViewModel.searchNewsPage == totalPages
                            if (isLastPage) {
                                binding.recyclerSearch.setPadding(0, 0, 0, 0)
                            }
                        }
                    }

                    is Resource.Error<*> -> {
                        hideProgressBar()
                        response.message?.let { message ->
                            Toast.makeText(activity, "Error:$message", Toast.LENGTH_LONG).show()
                            showErrorMessage(message)
                        }
                    }

                    is Resource.Loading -> {
                        showProgressBar()

                    }
                }
            })

        retryButton.setOnClickListener{
            if(binding.searchEdit.text.toString().isNotEmpty()){
                articlesViewModel.searchNews(binding.searchEdit.text.toString())
            } else {
                hideErrorMessage()
            }
        }
    }


    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemSearchError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemSearchError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                articlesViewModel.searchNews(binding.searchEdit.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }
    }

    private fun setupSearchRecycler() {
        articleAdapter = ArticleAdapter()
        binding.recyclerSearch.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }
}



