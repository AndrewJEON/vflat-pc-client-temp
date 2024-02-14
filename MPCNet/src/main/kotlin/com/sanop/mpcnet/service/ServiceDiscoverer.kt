package com.sanop.mpcnet.service

import com.sanop.mpcnet.data.Device
import kotlinx.coroutines.*
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

interface ServiceDiscovererCallbacks {
    fun onDeviceDiscoverRequested()
    fun onDeviceListUpdated(devices: List<Device>)
}

object ServiceDiscoverer {
    private var discoveryJob: Job? = null
    private val discoveredDevices: MutableList<Device> = mutableListOf()

    private var socket: DatagramSocket? = null
    private val discoveryMessage = """
        M-SEARCH * HTTP/1.1
        HOST: 239.255.255.250:1900
        MAN: "ssdp:discover"
        MX: 1
        ST: ssdp:all
        """.trimIndent().toByteArray()

    fun discover(port: Int, callbacks: ServiceDiscovererCallbacks) {
        if (discoveryJob?.isActive == true) {
            stop()
        }

        initializeSocket()
        discoveryJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                callbacks.onDeviceDiscoverRequested()
                val newDevices = fetchDevices(port)
                if (newDevices != discoveredDevices) {
                    discoveredDevices.clear()
                    discoveredDevices.addAll(newDevices)
                    callbacks.onDeviceListUpdated(newDevices)
                }
                delay(5000)
            }
        }
    }

    fun stop() {
        discoveredDevices.clear()
        discoveryJob?.cancel()
        discoveryJob = null
        socket?.close()
        socket = null
    }

    private fun initializeSocket() {
        socket?.close()
        socket = DatagramSocket(1398).apply {
            broadcast = true
        }
    }

    private suspend fun fetchDevices(port: Int): MutableList<Device> {
        val deviceList = mutableListOf<Device>()
        val pcNameGetterRegex = "SERVER: (.*?), UpnP/1.0".toRegex()
        val openPortGetterRegex = "PORT: (\\d+)".toRegex()

        withContext(Dispatchers.IO) {
            val address = InetAddress.getByName("239.255.255.250")
            val packet = DatagramPacket(discoveryMessage, discoveryMessage.size, address, port)
            val currentSocket = socket ?: return@withContext

            currentSocket.send(packet)

            try {
                currentSocket.soTimeout = 2000
                val buffer = ByteArray(1024)

                while (true) {
                    val responsePacket = DatagramPacket(buffer, buffer.size)
                    currentSocket.receive(responsePacket)

                    val receivedData = String(responsePacket.data, 0, responsePacket.length).trim()
                    val pcName = pcNameGetterRegex.find(receivedData)?.groups?.get(1)?.value
                        ?: responsePacket.address.hostName
                    val openPort = openPortGetterRegex.find(receivedData)?.groups?.get(1)?.value
                        ?: "9802"

                    val newDevice = Device(
                        name = pcName,
                        address = responsePacket.address.hostAddress,
                        port = openPort
                    )

                    if (newDevice !in deviceList) {
                        deviceList.add(newDevice)
                    }
                }
            } catch (e: SocketTimeoutException) {
                //pass
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return deviceList
    }

}
