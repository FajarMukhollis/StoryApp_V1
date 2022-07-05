package com.fajar.storyapp.view.story.camera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.fajar.storyapp.R
import com.fajar.storyapp.databinding.ActivityCameraBinding
import com.fajar.storyapp.utils.createFile
import com.fajar.storyapp.view.story.CreateStoryActivity

class Camera : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.captureImage.setOnClickListener { takePhoto() }
        //Penggunaan cameraSelector juga dapat diatur dalam onClick button SwitchCamera
        binding.switchCamera.setOnClickListener {
            cameraSelector =
                if (cameraSelector.equals(CameraSelector.DEFAULT_BACK_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA

            startCamera()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    //Mengambil gambar dari kamera
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        //Awal Function untuk takePicture
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                //onError untuk menangani jika terdapat kegagalan selama proses mengambil gambar
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@Camera,
                        getString(R.string.failed_take),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent()
                    intent.putExtra("picture", photoFile)
                    intent.putExtra(
                        "isBackCamera",
                        cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                    )
                    setResult(CreateStoryActivity.CAMERA_X_RESULT, intent)
                    finish()
                }
            }
        )
        //Akhir Function untuk takePicture
    }

    //Akhir dari kode untuk mengambil gambar dari kamera

    //Menampilkan kamera dalam aplikasi
    private var imageCapture: ImageCapture? = null
    //Inisialisasi disini untuk menentukan kamera depan atau kamera belakang
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            //imageCapture yang nantinya digunakan function takePhoto untuk mengambil gambar dari kamera
            imageCapture = ImageCapture.Builder().build()

            /*
            Try catch disini berfungsi untuk memastikan bahwa kesalahan selama menghubungkan
            cameraProvider dengan cameraSelector dan preview
             */
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                /*
                Jika terjadi kesalahan selama proses menghubungkan ketiga hal tersebut,
                gunakan catch untuk mengatasi exception yang terjadi
                 */
            } catch (exc: Exception) {
                Toast.makeText(this@Camera, "Gagal menampilkan kamera", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    //Akhir dari kode untuk menampilkan kamera di aplikasi
    private fun hideSystemUI() {
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