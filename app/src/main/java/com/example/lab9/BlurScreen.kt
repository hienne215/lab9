package com.example.lab9_24it270

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import coil.compose.AsyncImage

@Composable
fun BlurScreen(viewModel: BlurViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val workInfo by viewModel.workInfo.collectAsState(initial = null)

    // Lấy URI ảnh kết quả từ WorkManager nếu đã chạy xong
    val outputUriString = workInfo?.outputData?.getString("KEY_IMAGE_URI")
    val displayUri = if (!outputUriString.isNullOrEmpty() && workInfo?.state == WorkInfo.State.SUCCEEDED) {
        Uri.parse(outputUriString)
    } else {
        viewModel.imageUri
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            viewModel.setSourceImage(uri)
            // Reset trạng thái công việc cũ khi chọn ảnh mới để quay về ảnh gốc
            viewModel.workId = null 
        }
    )

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Hiển thị ảnh (Gốc hoặc Mờ)
        if (displayUri != null) {
            AsyncImage(
                model = displayUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Select Photo from Gallery")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Select Blur Amount", style = MaterialTheme.typography.titleMedium)

        val options = listOf("A little blurred", "More blurred", "The most blurred")
        options.forEachIndexed { index, text ->
            val level = index + 1
            Row(
                Modifier.fillMaxWidth().selectable(
                    selected = (viewModel.blurLevel == level),
                    onClick = { viewModel.blurLevel = level }
                ).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (viewModel.blurLevel == level),
                    onClick = { viewModel.blurLevel = level }
                )
                Text(text = text, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Trạng thái xử lý
        if (workInfo?.state == WorkInfo.State.RUNNING) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Applying blur filter...", modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (workInfo?.state == WorkInfo.State.SUCCEEDED && outputUriString != null) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(outputUriString))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Blurred Image"))
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Share / Save Image")
            }
        }

        Button(
            onClick = { viewModel.applyBlur() },
            enabled = viewModel.imageUri != null && workInfo?.state != WorkInfo.State.RUNNING,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A6A))
        ) {
            Text(if (workInfo?.state == WorkInfo.State.SUCCEEDED) "Blur Again" else "Start Blur")
        }
    }
}