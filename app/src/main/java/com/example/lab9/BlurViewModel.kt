package com.example.lab9_24it270

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class BlurViewModel(private val workManager: WorkManager) : ViewModel() {

    internal var workId by mutableStateOf<UUID?>(null)
    
    // URI của ảnh người dùng chọn
    var imageUri by mutableStateOf<Uri?>(null)

    val workInfo: Flow<WorkInfo?> get() = workId?.let { 
        workManager.getWorkInfoByIdFlow(it) 
    } ?: flowOf(null)

    var blurLevel by mutableStateOf(1)

    fun applyBlur() {
        val currentUri = imageUri ?: return

        // Tạo Data để truyền vào Worker
        val inputData = workDataOf(
            "IMAGE_URI" to currentUri.toString(),
            "BLUR_LEVEL" to blurLevel
        )

        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(inputData)
            .build()

        workId = blurRequest.id

        workManager.enqueueUniqueWork(
            "IMAGE_BLUR_WORK",
            ExistingWorkPolicy.REPLACE,
            blurRequest
        )
    }

    fun setSourceImage(uri: Uri?) {
        imageUri = uri
    }
}