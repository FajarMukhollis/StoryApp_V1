package com.fajar.storyapp.view.main

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.fajar.storyapp.api.ApiConfig
import com.fajar.storyapp.api.response.story.Story
import com.fajar.storyapp.api.response.story.StoryResponse
import com.fajar.storyapp.model.UserModel
import com.fajar.storyapp.model.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel (private val pref: UserPreference) : ViewModel() {
    private val listStory = MutableLiveData<ArrayList<Story>?>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setStory(token: String) {
        _isLoading.value = true
        Log.d(this@MainViewModel::class.java.simpleName, token)
        ApiConfig().getApiService().getStories(token = "Bearer $token")
            .enqueue(object : Callback<StoryResponse> {
                override fun onResponse(
                    call: Call<StoryResponse>,
                    response: Response<StoryResponse>
                ) {
                    if (response.code() == 200) {
                        listStory.postValue(response.body()?.listStory)
                    } else {
                        listStory.postValue(null)
                    }
                }

                override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                    listStory.postValue(null)
                }
            })
    }

    fun getStories(): MutableLiveData<ArrayList<Story>?> {
        return listStory
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logoutUser() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}