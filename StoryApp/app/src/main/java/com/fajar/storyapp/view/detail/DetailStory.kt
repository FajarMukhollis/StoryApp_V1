package com.fajar.storyapp.view.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.fajar.storyapp.R
import com.fajar.storyapp.api.response.story.Story
import com.fajar.storyapp.databinding.ActivityDetailStoryBinding
import com.fajar.storyapp.utils.helper.Helper.withDateFormat

class DetailStory : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupView() {
        val story = intent.getParcelableExtra<Story>("Story") as Story
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivStory)

        binding.tvUsername.text = story.name
        binding.tvTime.text = getString(R.string.date, story.createdAt.withDateFormat())
        binding.tvDescription.text = story.description
    }
}