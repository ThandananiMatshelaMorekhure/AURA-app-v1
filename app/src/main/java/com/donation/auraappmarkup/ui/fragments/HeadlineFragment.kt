package com.donation.auraappmarkup.ui.fragments

import android.content.Context
import android.os.Bundle
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
import com.donation.auraappmarkup.databinding.FragmentHeadlineBinding
import com.donation.auraappmarkup.ui.Articles
import com.donation.auraappmarkup.ui.ArticlesViewModel

class HeadlineFragment : Fragment(R.layout.fragment_headline) {

lateinit var articlesViewModel: ArticlesViewModel
lateinit var articleAdapter: ArticleAdapter
lateinit var retryButton: Button
lateinit var errorText: TextView
lateinit var itemheadlinesError: CardView
lateinit var binding: FragmentHeadlineBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlineBinding.bind(view)

        itemheadlinesError = view.findViewById(R.id.itemHeadlinesError)
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        articlesViewModel = (activity as Articles).articlesViewModel
        setupHeadlinesRecycler()

        articleAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
               putSerializable("articles", it)
            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articlesFragment, bundle)
        }

        articlesViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let {articleResponse ->
                        articleAdapter.differ.submitList(articleResponse.articles.toList())
                        val totalPages = articleResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = articlesViewModel.headlinesPage == totalPages
                        if (isLastPage){
                            binding.recyclerHeadlines.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error<*> -> {
hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "Error:$message",Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()

                }
            }
        })
        retryButton.setOnClickListener{
            articlesViewModel.getHeadlines("us")
        }

    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(){
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage(){
        itemheadlinesError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String){
        itemheadlinesError.visibility = View.VISIBLE
        errorText.text =  message
        isError = true
    }

    val scrollListener = object : RecyclerView.OnScrollListener(){
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
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                articlesViewModel.getHeadlines("us")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }
    }
    private fun setupHeadlinesRecycler(){
        articleAdapter = ArticleAdapter()
        binding.recyclerHeadlines.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlineFragment.scrollListener)
        }
    }
}