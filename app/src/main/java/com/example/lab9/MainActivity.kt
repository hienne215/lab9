package com.example.lab9_24it270

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.work.WorkManager
import com.example.lab9_24it270.ui.theme.Lab9_24IT270Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo WorkManager & ViewModel
        val workManager = WorkManager.getInstance(applicationContext)
        val viewModel = BlurViewModel(workManager)

        enableEdgeToEdge()
        setContent {
            Lab9_24IT270Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BlurScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
