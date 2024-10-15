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
            // Riceve i dati dell’immagine dal client e li converte in un array di byte
            val buffer = ByteArray(1024 * 1024)  // 1MB buffer, dimensione variabile a seconda della risoluzione
            var bytesRead: Int
            val byteArrayOutputStream = ByteArrayOutputStream()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }

            val imageData = byteArrayOutputStream.toByteArray()

            // Aggiorna l'immagine nel ViewModel
            viewModel.receiveFrame(imageData)

            // Chiudi la connessione
            inputStream.close()
            byteArrayOutputStream.close()
        }
    }
}