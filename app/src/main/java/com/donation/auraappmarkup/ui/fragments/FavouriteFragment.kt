package com.donation.auraappmarkup.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.donation.auraappmarkup.Adapter.ArticleAdapter
import com.donation.auraappmarkup.R
import com.donation.auraappmarkup.databinding.FragmentFavouriteBinding
import com.donation.auraappmarkup.ui.Articles
import com.donation.auraappmarkup.ui.ArticlesViewModel
import com.google.android.material.snackbar.Snackbar

class FavouriteFragment : Fragment(R.layout.fragment_favourite) {

    lateinit var articlesViewModel: ArticlesViewModel
    lateinit var articleAdapter: ArticleAdapter
    lateinit var binding: FragmentFavouriteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articlesViewModel = (activity as Articles).articlesViewModel
        setupFavouriteRecycler()

        articleAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("articles", it)
            }
            findNavController().navigate(R.id.action_favouritesFragment_to_articlesFragment, bundle)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = articleAdapter.differ.currentList[position]
                articlesViewModel.deleteArticle(article)
                Snackbar.make(view, " removed from favourites", Snackbar.LENGTH_LONG).apply {
                    setAction("Undone") {
                        articlesViewModel.addToFavourite(article)
                    }
                    show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerFavourites)

        articlesViewModel.getFavouriteNews().observe(viewLifecycleOwner) { articles ->
            articleAdapter.differ.submitList(articles)
        }
    }

    private fun setupFavouriteRecycler() {
        articleAdapter = ArticleAdapter()
        binding.recyclerFavourites.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}