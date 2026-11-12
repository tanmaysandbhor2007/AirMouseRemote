package com.airmouse.remote.ui

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.airmouse.remote.model.AirPointerSettings
import com.airmouse.remote.model.ConnectionState
import com.airmouse.remote.model.MouseButton
import com.airmouse.remote.model.TVKey
import com.airmouse.remote.model.TransportType
import com.airmouse.remote.sensor.GyroMotionEngine
import com.airmouse.remote.transport.InputDispatcher
import com.airmouse.remote.transport.bluetooth.BluetoothHidDispatcher
import com.airmouse.remote.transport.wifi.WifiUdpDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothDispatcher = BluetoothHidDispatcher(application, viewModelScope)
    private val wifiDispatcher = WifiUdpDispatcher(viewModelScope)

    private val _transportType = MutableStateFlow(TransportType.BLUETOOTH)
    val transportType: StateFlow<TransportType> = _transportType.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val connectionState: StateFlow<ConnectionState> = _transportType.flatMapLatest { type ->
        when (type) {
            TransportType.BLUETOOTH -> bluetoothDispatcher.connectionState
            TransportType.WIFI -> wifiDispatcher.connectionState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    private val _isAirPointerEnabled = MutableStateFlow(false)
    val isAirPointerEnabled: StateFlow<Boolean> = _isAirPointerEnabled.asStateFlow()

    private val _settings = MutableStateFlow(AirPointerSettings())
    val settings: StateFlow<AirPointerSettings> = _settings.asStateFlow()

    private val _bondedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val bondedDevices: StateFlow<List<BluetoothDevice>> = _bondedDevices.asStateFlow()

    val currentBluetoothDevice: BluetoothDevice?
        get() = bluetoothDispatcher.connectedDevice

    private val motionEngine = GyroMotionEngine(application) { dx, dy ->
        getActiveDispatcher().sendPointerDelta(dx, dy)
    }

    init {
        motionEngine.settings = _settings.value
        motionEngine.start()
    }

    fun onActivityResumed() {
        if (_transportType.value == TransportType.BLUETOOTH) {
            bluetoothDispatcher.start()
            bluetoothDispatcher.registerHidApp()
            refreshBondedDevices()
        }
    }

    fun onPermissionsGranted() {
        bluetoothDispatcher.start()
        bluetoothDispatcher.registerHidApp()
        refreshBondedDevices()
    }

    fun refreshBondedDevices() {
        _bondedDevices.value = bluetoothDispatcher.getBondedDevices()
    }

    fun connectBluetoothDevice(device: BluetoothDevice) {
        if (_transportType.value != TransportType.BLUETOOTH) {
            _transportType.value = TransportType.BLUETOOTH
        }
        bluetoothDispatcher.tryConnect(device)
    }

    fun disconnectBluetooth() {
        bluetoothDispatcher.disconnect()
    }

    private fun getActiveDispatcher(): InputDispatcher {
        return when (_transportType.value) {
            TransportType.BLUETOOTH -> bluetoothDispatcher
            TransportType.WIFI -> wifiDispatcher
        }
    }

    fun toggleTransport() {
        val current = _transportType.value
        val next = if (current == TransportType.BLUETOOTH) TransportType.WIFI else TransportType.BLUETOOTH
        _transportType.value = next

        if (next == TransportType.WIFI) {
            val s = _settings.value
            wifiDispatcher.updateTarget(s.wifiHostIp, s.wifiPort)
        } else {
            bluetoothDispatcher.start()
            refreshBondedDevices()
        }
    }

    fun toggleAirPointer() {
        val next = !_isAirPointerEnabled.value
        _isAirPointerEnabled.value = next
        motionEngine.isAirPointerEnabled = next
    }

    fun setClutchState(isDown: Boolean) {
        motionEngine.isClutchEngaged = isDown
    }

    fun sendMouseButton(button: MouseButton, isPressed: Boolean) {
        getActiveDispatcher().sendMouseButton(button, isPressed)
    }

    fun sendScroll(verticalDelta: Int) {
        getActiveDispatcher().sendScroll(verticalDelta)
    }

    fun sendTVKey(key: TVKey) {
        getActiveDispatcher().sendTVKey(key)
    }

    fun recenterGyro() {
        motionEngine.resetSmoothing()
    }

    fun updateSettings(newSettings: AirPointerSettings) {
        _settings.value = newSettings
        motionEngine.settings = newSettings
        if (_transportType.value == TransportType.WIFI) {
            wifiDispatcher.updateTarget(newSettings.wifiHostIp, newSettings.wifiPort)
        }
    }

    override fun onCleared() {
        super.onCleared()
        motionEngine.stop()
        bluetoothDispatcher.stop()
        wifiDispatcher.stop()
    }
}
