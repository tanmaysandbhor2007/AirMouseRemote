package com.airmouse.remote.model

enum class MouseButton(val bitMask: Byte) {
    LEFT(0x01),
    RIGHT(0x02),
    MIDDLE(0x04)
}

enum class TVKey {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    OK,
    BACK,
    HOME,
    MENU,
    VOLUME_UP,
    VOLUME_DOWN,
    MUTE,
    PLAY_PAUSE
}

enum class TransportType {
    BLUETOOTH,
    WIFI
}

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val targetName: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class AirPointerSettings(
    val sensitivity: Float = 1.2f,
    val deadzone: Float = 0.025f,
    val accelFactor: Float = 0.6f,
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    val holdToAim: Boolean = false, // false = toggle mode; true = hold thumb to aim
    val wifiHostIp: String = "192.168.1.100",
    val wifiPort: Int = 8888
)
