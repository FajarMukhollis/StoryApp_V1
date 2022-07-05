package com.fajar.storyapp.api.response.login

import com.fajar.storyapp.model.UserModel

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: UserModel
    )

