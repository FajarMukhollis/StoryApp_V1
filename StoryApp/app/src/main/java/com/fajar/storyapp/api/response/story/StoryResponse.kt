package com.fajar.storyapp.api.response.story

data class StoryResponse(
    val error: Boolean,
    val message: String,
    val listStory: ArrayList<Story>
)

