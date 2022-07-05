package com.fajar.storyapp.view.story

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.fajar.storyapp.R
import com.fajar.storyapp.view.ViewModelFactory
import com.fajar.storyapp.api.ApiConfig
import com.fajar.storyapp.api.response.UploadResponse
import com.fajar.storyapp.databinding.ActivityCreateStoryBinding
import com.fajar.storyapp.model.UserPreference
import com.fajar.storyapp.utils.rotateBitmap
import com.fajar.storyapp.utils.uriToFile
import com.fajar.storyapp.view.main.MainViewModel
import com.fajar.storyapp.view.story.camera.Camera
import com.fajar.storyapp.utils.reduceFileImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class CreateStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateStoryBinding
    private lateinit var viewModel: MainViewModel

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(this, getString(R.string.permission_no_granted), Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupViewModel()
        setMyButtonEnable()

        binding.openCamera.setOnClickListener { startCameraX() }
        binding.openFolder.setOnClickListener { startGallery() }
        binding.btnUpload.setOnClickListener { lifecycleScope.launch(Dispatchers.Main) { uploadImage() } }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setMyButtonEnable() {
        val desc = binding.edtDesc.text
        binding.btnUpload.isEnabled = desc != null
    }

    private fun setupViewModel() {
        val pref = UserPreference.getInstance(dataStore)
        viewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih gambar")
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage() {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val description =
                binding.edtDesc.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            binding.loadingProgress.visibility = View.VISIBLE

            viewModel.getUser().observe(this) { user ->
                val service = ApiConfig().getApiService()
                    .uploadImage(imageMultipart, description, "Bearer ${user.token}")
                service.enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(
                        call: Call<UploadResponse>,
                        response: Response<UploadResponse>
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null && !responseBody.error) {
                                Toast.makeText(
                                    this@CreateStoryActivity,
                                    getString(R.string.succes_upload),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@CreateStoryActivity,
                                response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        Toast.makeText(
                            this@CreateStoryActivity,
                            getString(R.string.retrofit_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        } else {
            Toast.makeText(
                this@CreateStoryActivity,
                getString(R.string.add_image_warn),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //Berpindah dari MainActivity ke CameraX
    private fun startCameraX() {
        val intent = Intent(this, Camera::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private var getFile: File? = null
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            val result = rotateBitmap(
                BitmapFactory.decodeFile(getFile?.path)
            )

            binding.ivStoryImage.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val contentResolver: ContentResolver = contentResolver
            val myFile = uriToFile(selectedImg, this@CreateStoryActivity)
            getFile = myFile
            binding.ivStoryImage.setImageURI(selectedImg)
        }
    }
}