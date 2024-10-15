package com.akhter.icastreceiver

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.example.icastreceiver.ScreenCastViewModel

@Composable
fun MainScreen(viewModel: ScreenCastViewModel) {
    val imageData by viewModel.imageData.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        imageData?.let { data ->
            // Converti `ByteArray` in `Bitmap`
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            bitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        } ?: run {
            Text(text = "In attesa dello screencast...", modifier = Modifier.align(Alignment.Center))
        }
    }
}