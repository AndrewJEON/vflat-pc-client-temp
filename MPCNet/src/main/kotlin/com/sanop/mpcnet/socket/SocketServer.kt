package com.sanop.mpcnet.socket

import com.sanop.mpcnet.common.Utils.toJsonString
import com.sanop.mpcnet.data.Command
import com.sanop.mpcnet.data.FileData
import com.sanop.mpcnet.socket.SocketHelper.readString
import com.sanop.mpcnet.socket.SocketHelper.receiveFile
import com.sanop.mpcnet.socket.SocketHelper.writeString
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*

object SocketServer {
    private lateinit var serverSocket: ServerSocket
    private lateinit var inputFileHandler: InputFileHandler
    private val clients = mutableListOf<Socket>()

    fun bindAndAccept(address: String, port: Int, handler: InputFileHandler) = runBlocking {
        inputFileHandler = handler
        serverSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(InetSocketAddress(address, port))
        println("Server started at ${serverSocket.localAddress}")

        while (true) {
            val client = serverSocket.accept()

            clients.add(client)
            println("Accepted connection from ${client.remoteAddress}")

            CoroutineScope(Dispatchers.IO).launch {
                handleClient(client)
            }
        }
    }

    private suspend fun handleClient(clientSocket: Socket) {
        val input = clientSocket.openReadChannel()
        val output = clientSocket.openWriteChannel(autoFlush = true)

        try {
            while (true) {
                val inputString = input.readString() ?: continue

                if (inputString.startsWith("@")) {
                    val command = Command.fromCommandString(inputString)

                    when (command) {
                        Command.FILE -> {
                            val fileData = input.receiveFile()
                            val response = if (fileData == null) {
                                mapOf("status" to "failed")
                            } else {
                                inputFileHandler.handle(fileData)
                            }.toJsonString()
                            output.writeString(response)
                        }
                        Command.PING -> {
                            output.writeString(Command.PONG.toCommandString())
                        }
                        Command.CLOSE -> {
                            break
                        }
                        else -> {
                            // Unknown command
                        }
                    }
                } else {
                   // Unknown command
                }

            }
        } catch (e: Exception) {
            // Handle any exceptions
            e.printStackTrace()
        } finally {
            clients.remove(clientSocket)
            withContext(Dispatchers.IO) {
                clientSocket.close()
            }
        }
    }

    fun closeConnection() {
        serverSocket.close()
        clients.forEach { it.close() }
        clients.clear()
    }

    fun interface InputFileHandler {
        fun handle(inputFileData: FileData): Map<String, String>
    }
}
