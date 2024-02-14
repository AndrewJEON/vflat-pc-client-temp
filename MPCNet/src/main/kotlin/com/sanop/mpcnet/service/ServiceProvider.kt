package com.sanop.mpcnet.service

import java.io.IOException
import java.net.*
import java.util.*

object ServiceProvider {
    private val pcName = InetAddress.getLocalHost().hostName
    private val uuid = UUID.randomUUID().toString()
    private var providerThread: Thread? = null
    private var socket: MulticastSocket? = null
    private const val discoverMethod = "M-SEARCH * HTTP/1.1"

    fun start(port: Int = 1398, receiveBufferSize: Int = 1024, openPort: Int = 9802) {
        if (providerThread != null || socket != null) return

        val responseMessage = """
            HTTP/1.1 200 OK
            ST: ssdp:all
            USN: uuid:$uuid::urn:schemas-upnp-org:device:ConnectionManager:1
            SERVER: $pcName, UpnP/1.0, MpcNet/1.0
            PORT: $openPort
        """.trimIndent().toByteArray()

        socket = MulticastSocket(port).apply {
            val groupAddress = InetAddress.getByName("239.255.255.250")
            val groupSocketAddress = InetSocketAddress(groupAddress, port)
            val localAddress = NetworkInterface.getNetworkInterfaces().asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .find { !it.isLoopbackAddress && it is Inet4Address }
            val networkInterface = NetworkInterface.getByInetAddress(localAddress)

            joinGroup(groupSocketAddress, networkInterface)
            broadcast = true
        }

        providerThread = Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val buffer = ByteArray(receiveBufferSize)
                    val packet = DatagramPacket(buffer, buffer.size)

                    socket?.receive(packet)

                    val requestString = String(packet.data).trim()

                    if (requestString.contains(discoverMethod)) {
                        val responsePacket = DatagramPacket(responseMessage, responseMessage.size, packet.address, packet.port)
                        socket?.send(responsePacket)
                    }
                }
            } catch (e: IOException) {
                // Handle exception or just print for now
                e.printStackTrace()
            } finally {
                socket?.close()
            }
        }.apply {
            start()
        }
    }

    fun stop() {
        providerThread?.interrupt()
        providerThread = null
        socket?.close()
        socket = null
    }
}