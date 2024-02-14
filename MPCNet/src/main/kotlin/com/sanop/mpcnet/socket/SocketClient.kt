package com.sanop.mpcnet.socket

import com.sanop.mpcnet.data.Command
import com.sanop.mpcnet.socket.SocketHelper.readString
import com.sanop.mpcnet.socket.SocketHelper.sendFile
import com.sanop.mpcnet.socket.SocketHelper.writeString
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

object SocketClient {
    private lateinit var socket: ChannelManageableSocket
    private var heartbeatJob: Job? = null

    private val mutableConnectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = mutableConnectionState

    private val socketMutex = Mutex()

    suspend fun connect(address: String, port: Int, onConnected: () -> Unit, onFailed: () -> Unit) {
        try {
            withTimeout(10_000) {
                socket = ChannelManageableSocket(aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress(address, port)))
                mutableConnectionState.emit(true)
                startHeartbeat()
                onConnected()
            }
        } catch (e: Exception) {
            mutableConnectionState.emit(false)
            onFailed()
        }
    }

    suspend fun closeConnection() {
        withContext(Dispatchers.IO) {
            if (mutableConnectionState.value) {
                socket.close()
            }
        }
        mutableConnectionState.emit(false)
    }

    suspend fun sendFile(filename: String, file: File, onSuccess: () -> Unit, onFailed: (String) -> Unit) {
        try {
            socketMutex.withLock {
                withTimeout(10_000) {
                    socket.sendFile(filename, file)
                    onSuccess()
                }
            }
        } catch (e: FileTransferException) {
            onFailed(e.message!!)
        } catch (e: Exception) {
            onFailed("Unknown Error")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = GlobalScope.launch(Dispatchers.IO) {
            while (mutableConnectionState.value) {
                delay(5000)
                try {
                    withTimeout(3000) {
                        socketMutex.withLock {
                            socket.sendPing()
                        }
                    }
                } catch (e: Exception) {
                    mutableConnectionState.value = false
                }
            }
        }
    }

    private suspend fun ChannelManageableSocket.sendPing() {
        writeChannel.writeString(Command.PING.toCommandString())
        val response = readChannel.readString()
        if (response != Command.PONG.toCommandString()) {
            throw Exception("Invalid response")
        }
    }
}
