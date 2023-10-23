package com.b1nar10.ml_face_recognition_tutorial.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.b1nar10.ml_face_recognition_tutorial.data.FaceAnalyzerRepository
import com.b1nar10.ml_face_recognition_tutorial.data.model.PersonModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceDetectorViewModel @Inject constructor(
    private val faceAnalyzerRepository: FaceAnalyzerRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(FaceRecognitionUiState())
    val uiState: StateFlow<FaceRecognitionUiState> = _uiState.asStateFlow()

    // Analyzes a given face image and updates the UI state based on the recognition result.
    fun analyzeFaceImage(bitmapImage: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val personModel = faceAnalyzerRepository.analyzeFaceImage(bitmapImage)

                // Update the UI state based on whether the person is recognized or unknown
                if (personModel.personName == "Unknown") {
                    _uiState.update {
                        it.copy(recognitionState = RecognitionState.Unknown(personModel.bitmapImage))
                    }
                } else if (personModel.personName.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            recognitionState = RecognitionState.Recognized(personModel.personName),
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle any exception that occurred during recognition
                _uiState.update {
                    it.copy(recognitionState = RecognitionState.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    // Saves a new face to the repository and resets the recognition state if successful
    fun saveNewFace(personModel: PersonModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSaved = faceAnalyzerRepository.saveNewFace(personModel)
                if (isSaved) onResetRecognitionState()
            } catch (e: Exception) {
                // Handle any exception that occurred during recognition
                _uiState.update {
                    it.copy(recognitionState = RecognitionState.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    // Resets the recognition state to idle
    fun onResetRecognitionState() {
        _uiState.update {
            it.copy(recognitionState = RecognitionState.Idle)
        }
    }
}