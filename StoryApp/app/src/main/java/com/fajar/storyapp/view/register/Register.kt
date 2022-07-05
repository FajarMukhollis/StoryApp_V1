package com.fajar.storyapp.view.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
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
import com.fajar.storyapp.R
import com.fajar.storyapp.api.ApiConfig
import com.fajar.storyapp.api.response.UploadResponse
import com.fajar.storyapp.api.response.register.Register
import com.fajar.storyapp.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun playAnimation() {

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1F).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.txtname, View.ALPHA, 1F).setDuration(500)
        val nameInput =
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.txtemail, View.ALPHA, 1F).setDuration(500)
        val emailInput =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(binding.txtpassword, View.ALPHA, 1F).setDuration(500)
        val passwordInput =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1F).setDuration(500)

        //Menampilkan animasi secara bergantian
        AnimatorSet().apply {
            playSequentially(
                title,
                name,
                nameInput,
                email,
                emailInput,
                password,
                passwordInput,
                button
            )
            start()
        }
    }

    private fun setupAction() {
        setMyButtonEnable()
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //Do Nothing
            }

            override fun afterTextChanged(s: Editable) {
                //Do Nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }
        })

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

        binding.btnRegister.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val pass = binding.passwordEditText.text.toString()

            binding.loadingProgress.visibility = View.VISIBLE
            ApiConfig().getApiService().signupUser(Register(name, email, pass))
                .enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(
                        call: Call<UploadResponse>,
                        response: Response<UploadResponse>
                    ) {
                        //201 berarti berhasil buat
                        if (response.code() == 201) {
                            binding.loadingProgress.visibility = View.INVISIBLE
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.success_signup),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            binding.loadingProgress.visibility = View.INVISIBLE
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.error_400),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        binding.loadingProgress.visibility = View.VISIBLE
                        Log.d("onFailure: ", t.message.toString())
                    }
                })
        }
    }

    private fun setMyButtonEnable() {
        val name = binding.nameEditText.text
        val email = binding.emailEditText.text
        val pass = binding.passwordEditText.text
        binding.btnRegister.isEnabled =
            email != null && email.toString().isNotEmpty() && pass != null && pass.toString()
                .isNotEmpty() && name != null && name.toString().isNotEmpty()
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