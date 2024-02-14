package com.sanop.mpcnet.socket

import io.ktor.network.sockets.*
import io.ktor.utils.io.*

class ChannelManageableSocket(private val delegate: Socket) : Socket by delegate {
    private var _readChannel: ByteReadChannel? = null
    private var _writeChannel: ByteWriteChannel? = null

    val readChannel: ByteReadChannel
        get() = _readChannel ?: openReadChannel().also { _readChannel = it }

    val writeChannel: ByteWriteChannel
        get() = _writeChannel ?: openWriteChannel(autoFlush = true).also { _writeChannel = it }
}
