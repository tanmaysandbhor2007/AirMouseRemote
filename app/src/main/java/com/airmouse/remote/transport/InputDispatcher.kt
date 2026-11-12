package com.airmouse.remote.transport

import com.airmouse.remote.model.ConnectionState
import com.airmouse.remote.model.MouseButton
import com.airmouse.remote.model.TVKey
import com.airmouse.remote.model.TransportType
import kotlinx.coroutines.flow.StateFlow

interface InputDispatcher {
    val transportType: TransportType
    val connectionState: StateFlow<ConnectionState>

    fun start()
    fun stop()

    fun sendPointerDelta(dx: Int, dy: Int)
    fun sendMouseButton(button: MouseButton, isPressed: Boolean)
    fun sendScroll(verticalDelta: Int)
    fun sendTVKey(key: TVKey)
    fun releaseAll()
}
