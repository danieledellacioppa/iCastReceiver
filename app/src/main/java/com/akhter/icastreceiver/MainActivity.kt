package com.akhter.icastreceiver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
            try {
                val serverSocket = ServerSocket(serverPort)
                println("Server in ascolto sulla porta $serverPort")
                while (true) {
                    val clientSocket = serverSocket.accept()
                    println("Connessione accettata da ${clientSocket.inetAddress}")
                    handleClient(clientSocket.getInputStream())
                }
            } catch (e: Exception) {
                println("Errore nel server: ${e.message}")
            }
        }
    }

    private fun handleClient(inputStream: InputStream) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sizeBuffer = ByteArray(4)
                while (true) {
                    // Leggi i primi 4 byte per la dimensione del frame
                    val bytesRead = inputStream.read(sizeBuffer)
                    if (bytesRead == -1) {
                        // Fine dello stream
                        println("Connessione chiusa dal client.")
                        break
                    } else if (bytesRead < 4) {
                        println("Impossibile leggere la dimensione del frame.")
                        break
                    }

                    val frameSize = ByteBuffer.wrap(sizeBuffer).order(ByteOrder.BIG_ENDIAN).int

                    // Verifica che la dimensione sia positiva
                    if (frameSize > 0) {
                        val buffer = ByteArray(frameSize)
                        var totalBytesRead = 0

                        while (totalBytesRead < frameSize) {
                            val readBytes = inputStream.read(buffer, totalBytesRead, frameSize - totalBytesRead)
                            if (readBytes == -1) {
                                println("Stream interrotto durante la lettura del frame.")
                                break
                            }
                            totalBytesRead += readBytes
                        }

                        if (totalBytesRead == frameSize) {
                            // Aggiorna l'immagine nel ViewModel
                            viewModel.receiveFrame(buffer)
                            println("Ricevuto frame con dimensione: $frameSize bytes")
                        } else {
                            println("Frame incompleto, bytes ricevuti: $totalBytesRead di $frameSize")
                            break
                        }
                    } else {
                        println("Errore: dimensione frame negativa o zero.")
                        break
                    }
                }
            } catch (e: Exception) {
                println("Errore durante la gestione del client: ${e.message}")
            } finally {
                inputStream.close()
            }
        }
    }
}