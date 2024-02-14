package com.sanop.mpcnet.socket

import com.sanop.mpcnet.common.Utils.jsonToMap
import com.sanop.mpcnet.data.Command
import com.sanop.mpcnet.data.FileData
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import java.io.File
import kotlin.text.String
import kotlin.text.toByteArray

class FileTransferException(message: String) : Exception(message)

object SocketHelper {
    suspend fun ChannelManageableSocket.sendFile(fileName: String, file: File): Map<String, String> {
        val input = readChannel
        val output = writeChannel
        output.run {
            writeString(Command.FILE.toCommandString())
            sendFile(fileName, file)
        }

        val response = input.readString()?.jsonToMap()

        if (response == null || response["status"] != "success") {
            val reason = response?.get("reason") ?: "Unknown Error"
            throw FileTransferException(reason)
        }

        return response
    }

    suspend fun ByteReadChannel.receiveFile(): FileData? {
        val fileName = readString() ?: return null
        val fileSize = readLong()
        val fileBytes = ByteArray(fileSize.toInt())
        readFully(fileBytes)

        File(fileName).writeBytes(fileBytes)

        return FileData(fileName, fileBytes)
    }

    suspend fun ByteReadChannel.readString(): String? {
        return try {
            val stringSize = readInt()
            val stringBytes = ByteArray(stringSize)
            readFully(stringBytes)
            String(stringBytes)
        } catch (e: EOFException) {
            Command.CLOSE.toCommandString()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ByteWriteChannel.sendFile(fileName: String, file: File) {
        val fileNameBytes = fileName.toByteArray(Charsets.UTF_8)
        val fileBytes = file.readBytes()

        writeInt(fileNameBytes.size)
        writeFully(fileNameBytes)
        writeLong(fileBytes.size.toLong())
        writeFully(fileBytes)
    }

    suspend fun ByteWriteChannel.writeString(string: String) {
        val stringBytes = string.toByteArray(Charsets.UTF_8)
        writeInt(stringBytes.size)
        writeFully(stringBytes)
    }
}
