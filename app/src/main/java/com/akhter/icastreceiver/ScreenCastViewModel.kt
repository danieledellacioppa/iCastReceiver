package com.akhter.icastreceiver

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenCastViewModel : ViewModel() {

    private val _imageData = MutableStateFlow<ByteArray?>(null)
    val imageData = _imageData.asStateFlow()

    fun receiveFrame(data: ByteArray) {
        _imageData.value = data
    }
}