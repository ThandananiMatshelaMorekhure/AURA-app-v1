package com.donation.auraappmarkup.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.donation.auraappmarkup.Profile
import com.donation.auraappmarkup.R
import com.donation.auraappmarkup.ToDoList
import com.donation.auraappmarkup.databinding.ActivityArticlesBinding
import com.donation.auraappmarkup.db.ArticleDatabase
import com.donation.auraappmarkup.repository.ArticleRepository

class Articles : AppCompatActivity() {

    lateinit var articlesViewModel: ArticlesViewModel
    private lateinit var binding: ActivityArticlesBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Initialize ViewModel + Repository
        val articleRepository = ArticleRepository(ArticleDatabase(this))
        val viewModelProviderFactory = ArticlesViewModelProviderFactory(application, articleRepository)
        articlesViewModel = ViewModelProvider(this, viewModelProviderFactory)
            .get(ArticlesViewModel::class.java)

        // ✅ Setup NavHostFragment for fragments
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // ✅ Remove the custom listener and use ONLY setupWithNavController
        binding.bottomNavigationView.setupWithNavController(navController)

        // ✅ Back arrow setup
        val backArrow: ImageView? = findViewById(R.id.back_arrow)
        backArrow?.setOnClickListener {
            finish()
        }

        // ✅ Custom top icons (Profile + Add)
        val btnProfile: ImageView? = findViewById(R.id.ic_profile)
        val btnAdd: ImageView? = findViewById(R.id.ic_add)

        btnProfile?.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }

        btnAdd?.setOnClickListener {
            startActivity(Intent(this, ToDoList::class.java))
        }
    }
}