package com.airmouse.remote.transport.wifi

import android.util.Log
import com.airmouse.remote.model.ConnectionState
import com.airmouse.remote.model.MouseButton
import com.airmouse.remote.model.TVKey
import com.airmouse.remote.model.TransportType
import com.airmouse.remote.transport.InputDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WifiUdpDispatcher(
    private val scope: CoroutineScope,
    var hostIp: String = "192.168.1.100",
    var port: Int = 8888
) : InputDispatcher {

    companion object {
        private const val TAG = "WifiUdpDispatcher"
    }

    override val transportType: TransportType = TransportType.WIFI

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: DatagramSocket? = null
    private var targetAddress: InetAddress? = null
    private var heartbeatJob: Job? = null

    override fun start() {
        scope.launch(Dispatchers.IO) {
            try {
                if (socket == null || socket?.isClosed == true) {
                    socket = DatagramSocket()
                }
                targetAddress = InetAddress.getByName(hostIp)
                _connectionState.value = ConnectionState.Connected("$hostIp:$port")

                // Start lightweight ping
                startHeartbeat()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start UDP socket", e)
                _connectionState.value = ConnectionState.Error("Wi-Fi Error: ${e.message}")
            }
        }
    }

    fun updateTarget(newHost: String, newPort: Int) {
        hostIp = newHost
        port = newPort
        start()
    }

    override fun stop() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing UDP socket", e)
        }
        socket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun sendPacket(payload: String) {
        val s = socket ?: return
        val addr = targetAddress ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val bytes = payload.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(bytes, bytes.size, addr, port)
                s.send(packet)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending UDP packet", e)
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                sendPacket("PING")
                delay(2000)
            }
        }
    }

    override fun sendPointerDelta(dx: Int, dy: Int) {
        sendPacket("M:$dx:$dy")
    }

    override fun sendMouseButton(button: MouseButton, isPressed: Boolean) {
        val state = if (isPressed) "1" else "0"
        sendPacket("B:${button.name}:$state")
    }

    override fun sendScroll(verticalDelta: Int) {
        sendPacket("S:$verticalDelta")
    }

    override fun sendTVKey(key: TVKey) {
        sendPacket("K:${key.name}")
    }

    override fun releaseAll() {
        sendPacket("RELEASE")
    }
}
