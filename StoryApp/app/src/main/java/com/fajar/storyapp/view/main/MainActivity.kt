package com.fajar.storyapp.view.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fajar.storyapp.R
import com.fajar.storyapp.view.ViewModelFactory
import com.fajar.storyapp.adapter.ViewAdapter
import com.fajar.storyapp.databinding.ActivityMainBinding
import com.fajar.storyapp.model.UserPreference
import com.fajar.storyapp.view.story.CreateStoryActivity
import com.fajar.storyapp.view.welcome.LoginOrRegister

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateStoryActivity::class.java)
            startActivity(intent)
        }

        setupViewModel()
        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            else -> true
        }
    }

    private fun initRecyclerView() {
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        adapter = ViewAdapter()
        binding.rvStory.adapter = adapter
    }

    private fun setupViewModel() {
        val pref = UserPreference.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if (!user.isLogin) {
                val intent = Intent(this@MainActivity, LoginOrRegister::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            showLoading(true)
            mainViewModel.setStory(token = user.token)
            binding.nameTextView.text = getString(R.string.greeting, user.name)
            binding.btnLogout.setOnClickListener {
                mainViewModel.logoutUser()
            }
        }

        mainViewModel.getStories().observe(this) {
            if (it != null) {
                adapter.setStoryList(it)
                adapter.notifyDataSetChanged()
                showLoading(false)
            } else {
                showLoading(false)
            }
        }
    }


    private fun showLoading(state: Boolean) {
        if (state) {
            binding.loadingProgress.visibility = View.VISIBLE
        } else {
            binding.loadingProgress.visibility = View.GONE
        }
    }
}