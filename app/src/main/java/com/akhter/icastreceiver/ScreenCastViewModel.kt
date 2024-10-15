package com.example.icastreceiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScreenCastViewModel : ViewModel() {

    private val _imageData = MutableStateFlow<ByteArray?>(null)
    val imageData = _imageData.asStateFlow()

    fun receiveFrame(data: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageData.value = data
        }
    }
}