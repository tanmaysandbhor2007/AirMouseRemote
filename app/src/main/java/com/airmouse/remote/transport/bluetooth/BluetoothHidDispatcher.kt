package com.airmouse.remote.transport.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.airmouse.remote.model.ConnectionState
import com.airmouse.remote.model.MouseButton
import com.airmouse.remote.model.TVKey
import com.airmouse.remote.model.TransportType
import com.airmouse.remote.transport.InputDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class BluetoothHidDispatcher(
    private val context: Context,
    private val scope: CoroutineScope
) : InputDispatcher {

    companion object {
        private const val TAG = "BTHidDispatcher"
    }

    override val transportType: TransportType = TransportType.BLUETOOTH

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null
    var connectedDevice: BluetoothDevice? = null
        private set
    var isAppRegistered: Boolean = false
        private set

    private var isProxyBinding: Boolean = false
    private var isRegistering: Boolean = false

    private var activeMouseButtonsMask: Byte = 0
    private val executor = Executors.newSingleThreadExecutor()

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            isAppRegistered = registered
            isRegistering = false
            Log.d(TAG, "HID App registration: registered=$registered, pluggedDevice=${pluggedDevice?.name}")
            if (registered && pluggedDevice != null) {
                tryConnect(pluggedDevice)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            val devName = device?.name ?: device?.address ?: "Target Device"
            Log.d(TAG, "HID Connection state: device=$devName, state=$state")

            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    _connectionState.value = ConnectionState.Connected(devName)
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = ConnectionState.Connecting
                }
                BluetoothProfile.STATE_DISCONNECTED, BluetoothProfile.STATE_DISCONNECTING -> {
                    if (connectedDevice == device || connectedDevice == null) {
                        connectedDevice = null
                        _connectionState.value = ConnectionState.Disconnected
                    }
                }
            }
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID Profile proxy connected")
                isProxyBinding = false
                hidDevice = proxy as? BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID Profile proxy disconnected")
                isProxyBinding = false
                hidDevice = null
                isAppRegistered = false
                isRegistering = false
                connectedDevice = null
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    override fun start() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = ConnectionState.Error("Bluetooth is disabled")
            return
        }

        if (hidDevice != null) {
            Log.d(TAG, "HID proxy already acquired, registering app...")
            registerHidApp()
            return
        }

        if (isProxyBinding) {
            Log.d(TAG, "HID proxy binding in progress...")
            return
        }

        isProxyBinding = true
        try {
            bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            isProxyBinding = false
            Log.e(TAG, "Failed to get HID proxy", e)
            _connectionState.value = ConnectionState.Error("Init error: ${e.message}")
        }
    }

    override fun stop() {
        try {
            hidDevice?.let { dev ->
                if (isAppRegistered) {
                    dev.unregisterApp()
                }
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, dev)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping HID service", e)
        }
        hidDevice = null
        connectedDevice = null
        isAppRegistered = false
        isRegistering = false
        isProxyBinding = false
        _connectionState.value = ConnectionState.Disconnected
    }

    fun registerHidApp() {
        val dev = hidDevice
        if (dev == null) {
            Log.d(TAG, "registerHidApp: hidDevice is null, calling start()")
            start()
            return
        }
        if (isAppRegistered) {
            Log.d(TAG, "registerHidApp: already registered")
            return
        }
        if (isRegistering) {
            Log.d(TAG, "registerHidApp: registration attempt already running")
            return
        }
        isRegistering = true

        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Air Mouse & TV Remote",
            "Universal Gyroscope Air Mouse and TV Remote",
            "AirMouse",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            HidReportDescriptor.DESCRIPTOR
        )

        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            9,
            0,
            11250,
            BluetoothHidDeviceAppQosSettings.MAX
        )

        scope.launch(Dispatchers.Main) {
            var attempts = 0
            while (!isAppRegistered && attempts < 5) {
                attempts++
                try {
                    val success = dev.registerApp(sdpSettings, qosSettings, qosSettings, executor, hidCallback)
                    Log.d(TAG, "registerApp attempt #$attempts returned: $success")
                    if (success) {
                        break
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "registerApp attempt #$attempts threw exception", e)
                }
                delay(800)
            }
            if (!isAppRegistered) {
                isRegistering = false
            }
        }
    }

    fun getBondedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bonded devices", e)
            emptyList()
        }
    }

    fun tryConnect(device: BluetoothDevice) {
        val dev = hidDevice
        if (dev == null) {
            Log.w(TAG, "Cannot connect: hidDevice is null. Re-initializing...")
            start()
            return
        }
        if (!isAppRegistered) {
            Log.d(TAG, "HID app not registered yet. Calling registerHidApp before connecting...")
            registerHidApp()
        }
        try {
            Log.d(TAG, "Calling hidDevice.connect to ${device.name ?: device.address}")
            _connectionState.value = ConnectionState.Connecting
            val res = dev.connect(device)
            Log.d(TAG, "hidDevice.connect returned: $res")
            if (!res) {
                _connectionState.value = ConnectionState.Error("Connection request rejected by system")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to device ${device.address}", e)
            _connectionState.value = ConnectionState.Error("Connect failed: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            connectedDevice?.let { dev ->
                hidDevice?.disconnect(dev)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
        connectedDevice = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sendPointerDelta(dx: Int, dy: Int) {
        val target = connectedDevice ?: return
        val report = HidReportDescriptor.createMouseReport(activeMouseButtonsMask, dx, dy, 0)
        try {
            hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_MOUSE.toInt(), report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send mouse report", e)
        }
    }

    override fun sendMouseButton(button: MouseButton, isPressed: Boolean) {
        val target = connectedDevice ?: return
        activeMouseButtonsMask = if (isPressed) {
            (activeMouseButtonsMask.toInt() or button.bitMask.toInt()).toByte()
        } else {
            (activeMouseButtonsMask.toInt() and button.bitMask.toInt().inv()).toByte()
        }
        val report = HidReportDescriptor.createMouseReport(activeMouseButtonsMask, 0, 0, 0)
        try {
            hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_MOUSE.toInt(), report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send mouse button", e)
        }
    }

    override fun sendScroll(verticalDelta: Int) {
        val target = connectedDevice ?: return
        val report = HidReportDescriptor.createMouseReport(activeMouseButtonsMask, 0, 0, verticalDelta)
        try {
            hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_MOUSE.toInt(), report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send scroll report", e)
        }
    }

    override fun sendTVKey(key: TVKey) {
        val target = connectedDevice ?: return

        val consumerMask = HidReportDescriptor.mapTVKeyToConsumer(key)
        if (consumerMask != null) {
            scope.launch(Dispatchers.IO) {
                val pressReport = byteArrayOf(consumerMask)
                hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_CONSUMER.toInt(), pressReport)
                delay(30)
                val releaseReport = byteArrayOf(0x00)
                hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_CONSUMER.toInt(), releaseReport)
            }
            return
        }

        val keyCode = HidReportDescriptor.mapTVKeyToKeyboard(key)
        if (keyCode != null) {
            scope.launch(Dispatchers.IO) {
                val pressReport = byteArrayOf(0x00, 0x00, keyCode, 0x00, 0x00, 0x00, 0x00, 0x00)
                hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_KEYBOARD.toInt(), pressReport)
                delay(30)
                val releaseReport = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
                hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_KEYBOARD.toInt(), releaseReport)
            }
        }
    }

    override fun releaseAll() {
        val target = connectedDevice ?: return
        activeMouseButtonsMask = 0
        try {
            hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_MOUSE.toInt(), byteArrayOf(0, 0, 0, 0))
            hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_CONSUMER.toInt(), byteArrayOf(0))
            hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_KEYBOARD.toInt(), ByteArray(8))
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing all keys", e)
        }
    }
}
