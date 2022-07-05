package com.fajar.storyapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.fajar.storyapp.R
import com.fajar.storyapp.view.ViewModelFactory
import com.fajar.storyapp.api.ApiConfig
import com.fajar.storyapp.api.response.login.LoginResponse
import com.fajar.storyapp.databinding.ActivityLoginBinding
import com.fajar.storyapp.model.UserModel
import com.fajar.storyapp.api.response.login.Login
import com.fajar.storyapp.model.UserPreference
import com.fajar.storyapp.view.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()
        setupView()
        setupAction()
        setupViewModel()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imgDicodingLogo, View.TRANSLATION_X, -30F, 30F).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            startDelay = 500
        }.start()

        val email = ObjectAnimator.ofFloat(binding.txtEmail, View.ALPHA, 1F).setDuration(500)
        val emailInput =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(binding.txtPassword, View.ALPHA, 1F).setDuration(500)
        val passwordInput =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1F).setDuration(500)

        AnimatorSet().apply {
            playSequentially (email, emailInput, password, passwordInput, button)
            start()
        }
    }

    private fun setupAction() {
        setMyButtonEnable()
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }
        })

        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }
        })

        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            binding.loadingProgress.visibility = View.VISIBLE
            ApiConfig().getApiService().loginUser(Login(email, password))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.code() == 200) {
                            val body = response.body()?.loginResult as UserModel

                            loginViewModel.saveUser(
                                UserModel(
                                    body.userId,
                                    body.name,
                                    body.token,
                                    isLogin = true
                                )
                            )
                            binding.loadingProgress.visibility = View.INVISIBLE

                            Toast.makeText(
                                applicationContext,
                                getString(R.string.succes_login),
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@Login, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            binding.loadingProgress.visibility = View.INVISIBLE

                            Toast.makeText(
                                applicationContext,
                                getString(R.string.error_401),
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d(
                                Login::class.java.simpleName,
                                response.body()?.message.toString()
                            )
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Log.d("failure: ", t.message.toString())
                    }
                })
        }
    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]

        loginViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    private fun setMyButtonEnable() {
        val email = binding.emailEditText.text
        val pass = binding.passwordEditText.text
        binding.btnLogin.isEnabled =
            email != null && email.toString().isNotEmpty() && pass != null && pass.toString()
                .isNotEmpty()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}