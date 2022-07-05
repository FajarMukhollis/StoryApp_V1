package com.fajar.storyapp.api

import com.fajar.storyapp.api.response.UploadResponse
import com.fajar.storyapp.api.response.login.Login
import com.fajar.storyapp.api.response.login.LoginResponse
import com.fajar.storyapp.api.response.register.Register
import com.fajar.storyapp.api.response.story.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("login")
    fun loginUser(
        @Body login: Login
    ): Call<LoginResponse>

    @POST("register")
    fun signupUser(
        @Body register: Register
    ): Call<UploadResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String
    ): Call<StoryResponse>

    @Multipart
    @POST("/v1/stories")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Header("Authorization") token: String
    ): Call<UploadResponse>

}