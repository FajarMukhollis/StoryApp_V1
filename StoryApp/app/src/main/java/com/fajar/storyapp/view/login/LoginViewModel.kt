package com.fajar.storyapp.view.login

import androidx.lifecycle.*
import com.fajar.storyapp.model.UserModel
import com.fajar.storyapp.model.UserPreference
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun saveUser(user: UserModel) {
        viewModelScope.launch {
            pref.saveUser(UserModel(user.userId, user.name, user.token, user.isLogin))
        }
    }
}