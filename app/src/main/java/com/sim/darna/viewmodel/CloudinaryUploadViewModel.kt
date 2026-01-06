package com.sim.darna.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.remote.CloudinaryApi
import com.sim.darna.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CloudinaryUploadViewModel @Inject constructor(
    private val cloudinaryApi: CloudinaryApi
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun uploadImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                // Using the centralized utility function
                val imagePart = ImageUtils.imageUriToMultipartPart(context, imageUri, "file")
                
                if (imagePart != null) {
                    val response = cloudinaryApi.uploadImage(imagePart)
                    if (response.isSuccessful && response.body() != null) {
                        _uploadState.value = UploadState.Success(response.body()!!.url)
                    } else {
                        _uploadState.value = UploadState.Error("Upload failed: ${response.code()} ${response.message()}")
                    }
                } else {
                    _uploadState.value = UploadState.Error("Could not process image file")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Exception: ${e.localizedMessage}")
            }
        }
    }

    // This function is kept for backward compatibility if needed elsewhere
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val timeStamp = System.currentTimeMillis()
            val tempFile = File(context.cacheDir, "upload_$timeStamp.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val imageUrl: String) : UploadState()
    data class Error(val message: String) : UploadState()
}