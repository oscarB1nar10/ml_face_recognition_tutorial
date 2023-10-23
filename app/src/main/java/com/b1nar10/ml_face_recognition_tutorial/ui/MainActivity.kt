package com.b1nar10.ml_face_recognition_tutorial.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.b1nar10.ml_face_recognition_tutorial.data.model.PersonModel
import com.b1nar10.ml_face_recognition_tutorial.databinding.ActivityMainBinding
import com.b1nar10.ml_face_recognition_tutorial.ui.utils.FaceContourGraphic
import com.b1nar10.ml_face_recognition_tutorial.ui.utils.saveBitmapToTempFile
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Inject the TextToSpeech instance using Dagger Hilt
    @Inject
    lateinit var textToSpeech: TextToSpeech

    @Inject
    lateinit var faceDetector: FaceDetector

    @Inject
    lateinit var cameraExecutor: ExecutorService

    private lateinit var faceDetection: FaceDetection

    lateinit var viewBinding: ActivityMainBinding
    private val viewModel: FaceDetectorViewModel by viewModels()

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissons ->
            // Handle permission granted/rejected
            var permissionGranted = true
            permissons.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }

            if (!permissionGranted) {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        checkPermissionsStatus()
        // Observe changes in the UI state
        observeUiState()
        // Configure UI elements and utilities
        configureUi()
    }

    private fun checkPermissionsStatus() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect{ uiState ->
                handleUiState(uiState)
            }
        }
    }

    private fun configureUi() {
        // Configure face detection utility
        faceDetection = FaceDetection(
            activity = this,
            faceDetector = faceDetector,
            onFacesDetected = ::onFacesDetected,
            onFaceCropped = ::onFaceCropped
        )
    }

    private fun handleUiState(uiState: FaceRecognitionUiState) {
        when(uiState.recognitionState) {
            is RecognitionState.Idle -> {
                startFaceDetector()
            }

            is RecognitionState.Recognized -> {
                // Handle recognized state
                val name = uiState.recognitionState.name
                showPersonName(name)
            }

            is RecognitionState.Unknown -> {
                // Handle unknown state
                uiState.recognitionState.faceBitmap?.let { showNameInputDialog(it) }
            }

            is RecognitionState.Error -> {
                // Handle error state
                val errorMessage = uiState.recognitionState.message
                showErrorDialog(errorMessage)
            }

            is RecognitionState.None -> {}
        }
    }

    private fun startFaceDetector(delayToStartRecognition: Long = 1000) {
        // Wait for 1 second before restarting face analysis
        Handler(Looper.getMainLooper()).postDelayed({
            println("startFaceDetector")
            faceDetection.start()
        }, delayToStartRecognition)
    }

    private fun showPersonName(name: String) {
        showToast("Hello $name")
        speakText("Hello $name")
        startFaceDetector()
    }

    private fun showNameInputDialog(faceBitmap: Bitmap) {
        val dialog = InputDetailsDialogFragment()
        val bitmapPath =  saveBitmapToTempFile(faceBitmap, this)
        val args = Bundle()
        args.putString("bitmap_path", bitmapPath)
        dialog.arguments = args

        dialog.setListener(object : InputDetailsDialogFragment.InputDetailsListener{
            override fun onDetailsEntered(id: String, name: String) {
                viewModel.saveNewFace(
                    PersonModel(
                        id = id,
                        personName = name,
                        bitmapImage = faceBitmap
                    )
                )
            }

            override fun cancel() {
                viewModel.onResetRecognitionState()
            }

        })

        dialog.show(supportFragmentManager, "InputDetailsDialogFragment")
    }

    private fun showErrorDialog(errorMessage: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(errorMessage)
            .setPositiveButton("Ok") {dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun onFacesDetected(faces: List<Face>) = with(viewBinding.graphicOverlay) {
        // Task completed successfully
        if (faces.isEmpty()) {
            showToast("No face found")
            return
        }

        this.clear()
        for (i in faces.indices) {
            val face = faces[i]
            val faceGraphic = FaceContourGraphic(this)
            this.add(faceGraphic)
            faceGraphic.updatedFace(face)
        }
    }

    // Handle cropped face images
    private fun onFaceCropped(face: Bitmap) {
        faceDetection.stop()
        viewModel.analyzeFaceImage(face)
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Start the camera and binds its lifecycle to the activity
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(IMAGE_RESOLUTION)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, faceDetection)
                }

            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind the use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun speakText(text: String) {
        val params = Bundle()
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "id")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }

        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "ml-face-recognition-tutorial"
        private val IMAGE_RESOLUTION = Size(1280, 720)
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA)
            .apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}