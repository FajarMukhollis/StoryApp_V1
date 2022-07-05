package com.fajar.storyapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fajar.storyapp.api.response.story.Story
import com.fajar.storyapp.databinding.ItemListStoryBinding
import com.fajar.storyapp.utils.helper.Helper.withDateFormat
import com.fajar.storyapp.view.detail.DetailStory

class ViewAdapter : RecyclerView.Adapter<ViewAdapter.MainViewHolder>() {
    inner class MainViewHolder(private val binding: ItemListStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Story) {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStory::class.java)
                intent.putExtra("Story", data)

                itemView.context.startActivity(
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(itemView.context as Activity)
                        .toBundle()
                )
            }

            binding.apply {
                tvUsername.text = data.name
                tvTime.text = data.createdAt.withDateFormat()
                tvDescription.text = data.description

                Glide.with(itemView)
                    .load(data.photoUrl)
                    .into(imgItemPhoto)
            }
        }
    }

    private var storyList: List<Story>? = null

    fun setStoryList(storyList: List<Story>?) {
        this.storyList = storyList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewAdapter.MainViewHolder {
        val view = ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(storyList?.get(position)!!)
    }

    override fun getItemCount(): Int {
        return if (storyList == null) 0
        else storyList?.size!!
    }
}