package com.akhter.icastreceiver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.icastreceiver.ScreenCastViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.ServerSocket
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ScreenCastViewModel
    private val serverPort = 7000  // Assicurati che corrisponda a quello impostato su iOS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(ScreenCastViewModel::class.java)

        setContent {
            MainScreen(viewModel = viewModel)
        }

        startServer()
    }

    private fun startServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(serverPort)
            while (true) {
                val clientSocket = serverSocket.accept()
                handleClient(clientSocket.getInputStream())
            }
        }
    }

    private fun handleClient(inputStream: InputStream) {
        lifecycleScope.launch(Dispatchers.IO) {
            val byteArrayOutputStream = ByteArrayOutputStream()

            // Leggi i primi 4 byte per la dimensione del frame
            val sizeBuffer = ByteArray(4)
            if (inputStream.read(sizeBuffer) == 4) {
                val frameSize = java.nio.ByteBuffer.wrap(sizeBuffer).int

                // Verifica che la dimensione sia positiva
                if (frameSize > 0) {
                    val buffer = ByteArray(frameSize)
                    var bytesRead: Int = 0
                    var totalBytesRead = 0

                    while (totalBytesRead < frameSize && inputStream.read(buffer, totalBytesRead, frameSize - totalBytesRead).also { bytesRead = it } != -1) {
                        totalBytesRead += bytesRead
                    }

                    // Aggiorna l'immagine nel ViewModel solo quando il frame è completo
                    if (totalBytesRead == frameSize) {
                        viewModel.receiveFrame(buffer)
                        println("Ricevuto frame con dimensione: $frameSize bytes")
                    } else {
                        println("Frame incompleto, bytes ricevuti: $totalBytesRead di $frameSize")
                    }
                } else {
                    println("Errore: dimensione frame negativa o zero.")
                }
            }
            inputStream.close()
        }
    }
}