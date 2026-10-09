package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.Executor

sealed interface CameraEvent {
    data class ImageCaptured(val bitmap: Bitmap) : CameraEvent
    data class Error(val message: String) : CameraEvent
}

class CameraViewModel : ViewModel() {

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    private val _isFlashEnabled = MutableStateFlow(false)
    val isFlashEnabled: StateFlow<Boolean> = _isFlashEnabled.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _cameraEvents = MutableSharedFlow<CameraEvent>()
    val cameraEvents: SharedFlow<CameraEvent> = _cameraEvents.asSharedFlow()

    private var imageCapture: ImageCapture? = null

    fun setImageCapture(capture: ImageCapture?) {
        this.imageCapture = capture
    }

    fun toggleCameraFacing() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    fun toggleFlash() {
        val newState = !_isFlashEnabled.value
        _isFlashEnabled.value = newState
        imageCapture?.flashMode = if (newState) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    fun takePhoto(context: Context, executor: Executor = ContextCompat.getMainExecutor(context)) {
        val capture = imageCapture ?: run {
            viewModelScope.launch {
                _cameraEvents.emit(CameraEvent.Error("الكاميرا غير جاهزة حالياً"))
            }
            return
        }

        _isCapturing.value = true

        capture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    _isCapturing.value = false
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    if (bitmap != null) {
                        viewModelScope.launch {
                            _cameraEvents.emit(CameraEvent.ImageCaptured(bitmap))
                        }
                    } else {
                        viewModelScope.launch {
                            _cameraEvents.emit(CameraEvent.Error("تعذر تحويل الصورة الملتقطة"))
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                    viewModelScope.launch {
                        _cameraEvents.emit(CameraEvent.Error(exception.message ?: "فشل التقاط الصورة"))
                    }
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

        val rotationDegrees = image.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
}
