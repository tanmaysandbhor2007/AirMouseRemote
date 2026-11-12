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
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var isAppRegistered = false

    private var activeMouseButtonsMask: Byte = 0
    private val executor = Executors.newSingleThreadExecutor()

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            isAppRegistered = registered
            Log.d(TAG, "HID App registration changed: registered=$registered, pluggedDevice=$pluggedDevice")
            if (registered && pluggedDevice != null) {
                // If there's an already plugged/paired device, initiate connection
                tryConnect(pluggedDevice)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            Log.d(TAG, "Connection state changed: device=${device?.name ?: device?.address}, state=$state")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    _connectionState.value = ConnectionState.Connected(device?.name ?: device?.address ?: "Connected Device")
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = ConnectionState.Connecting
                }
                BluetoothProfile.STATE_DISCONNECTED, BluetoothProfile.STATE_DISCONNECTING -> {
                    if (connectedDevice == device) {
                        connectedDevice = null
                    }
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID Profile proxy connected")
                hidDevice = proxy as? BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID Profile proxy disconnected")
                hidDevice = null
                isAppRegistered = false
                connectedDevice = null
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    override fun start() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = ConnectionState.Error("Bluetooth is disabled or not available")
            return
        }

        try {
            bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get HID proxy", e)
            _connectionState.value = ConnectionState.Error("Failed to init Bluetooth HID: ${e.message}")
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
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun registerHidApp() {
        val dev = hidDevice ?: return
        if (isAppRegistered) return

        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Air Mouse & TV Remote",
            "Universal Gyroscope Air Mouse and Smart TV Remote",
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

        try {
            dev.registerApp(sdpSettings, qosSettings, qosSettings, executor, hidCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException registering HID app (permission missing)", e)
            _connectionState.value = ConnectionState.Error("Bluetooth permission required")
        } catch (e: Exception) {
            Log.e(TAG, "Exception registering HID app", e)
            _connectionState.value = ConnectionState.Error("HID Register error: ${e.message}")
        }
    }

    fun tryConnect(device: BluetoothDevice) {
        try {
            hidDevice?.connect(device)
            _connectionState.value = ConnectionState.Connecting
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to device ${device.address}", e)
        }
    }

    fun disconnect() {
        try {
            connectedDevice?.let { dev -> hidDevice?.disconnect(dev) }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
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

        // Check if key maps to Consumer Control (Volume, Play/Pause, Home)
        val consumerMask = HidReportDescriptor.mapTVKeyToConsumer(key)
        if (consumerMask != null) {
            scope.launch(Dispatchers.IO) {
                // Press
                val pressReport = byteArrayOf(consumerMask)
                hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_CONSUMER.toInt(), pressReport)
                delay(30)
                // Release
                val releaseReport = byteArrayOf(0x00)
                hidDevice?.sendReport(target, HidReportDescriptor.REPORT_ID_CONSUMER.toInt(), releaseReport)
            }
            return
        }

        // Check if key maps to Keyboard (D-Pad Arrows, Enter, Back)
        val keyCode = HidReportDescriptor.mapTVKeyToKeyboard(key)
        if (keyCode != null) {
            scope.launch(Dispatchers.IO) {
                // Keyboard report format: [modifier, reserved, key1, key2, key3, key4, key5, key6]
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
