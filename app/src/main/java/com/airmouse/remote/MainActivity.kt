package com.airmouse.remote

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airmouse.remote.ui.MainViewModel
import com.airmouse.remote.ui.components.AirPointerPad
import com.airmouse.remote.ui.components.BluetoothDeviceDialog
import com.airmouse.remote.ui.components.ConnectionHeader
import com.airmouse.remote.ui.components.RemoteDPad
import com.airmouse.remote.ui.components.SettingsDialog
import com.airmouse.remote.ui.components.TVControlsRow
import com.airmouse.remote.ui.theme.AirMouseTheme
import com.airmouse.remote.ui.theme.BgDark

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AirMouseTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onActivityResumed()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val transportType by viewModel.transportType.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isAirPointerEnabled by viewModel.isAirPointerEnabled.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val bondedDevices by viewModel.bondedDevices.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showBluetoothDialog by remember { mutableStateOf(false) }

    // Discoverable launcher
    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Toast.makeText(context, "Phone is discoverable for 120s! Scan from your PC or TV.", Toast.LENGTH_LONG).show()
        viewModel.refreshBondedDevices()
    }

    // Bluetooth Permission handling for Android 12+ (API 31+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        } else {
            viewModel.onPermissionsGranted()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .systemBarsPadding(),
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Connectivity Status & Air Pointer Toggle
            ConnectionHeader(
                transportType = transportType,
                connectionState = connectionState,
                isAirPointerEnabled = isAirPointerEnabled,
                onToggleTransport = { viewModel.toggleTransport() },
                onToggleAirPointer = { viewModel.toggleAirPointer() },
                onOpenSettings = { showSettingsDialog = true },
                onOpenBluetoothDevices = {
                    viewModel.refreshBondedDevices()
                    showBluetoothDialog = true
                }
            )

            // Middle Section: Dynamic Switch between D-Pad and Air Pointer Pad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isAirPointerEnabled,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "control_mode_transition"
                ) { airPointerActive ->
                    if (airPointerActive) {
                        AirPointerPad(
                            isAirPointerEnabled = true,
                            isHoldToAim = settings.holdToAim,
                            onClutchStateChanged = { isEngaged ->
                                viewModel.setClutchState(isEngaged)
                            },
                            onMouseButton = { button, isPressed ->
                                viewModel.sendMouseButton(button, isPressed)
                            },
                            onScroll = { delta ->
                                viewModel.sendScroll(delta)
                            },
                            onRecenter = {
                                viewModel.recenterGyro()
                            }
                        )
                    } else {
                        RemoteDPad(
                            onKeyPress = { key ->
                                viewModel.sendTVKey(key)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Section: Persistent TV / Media Controls
            TVControlsRow(
                modifier = Modifier.padding(bottom = 16.dp),
                onKeyPress = { key ->
                    viewModel.sendTVKey(key)
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentSettings = settings,
                onDismiss = { showSettingsDialog = false },
                onSave = { updated ->
                    viewModel.updateSettings(updated)
                    showSettingsDialog = false
                }
            )
        }

        if (showBluetoothDialog) {
            BluetoothDeviceDialog(
                connectionState = connectionState,
                bondedDevices = bondedDevices,
                onDismiss = { showBluetoothDialog = false },
                onRefresh = { viewModel.refreshBondedDevices() },
                onConnectDevice = { device ->
                    viewModel.connectBluetoothDevice(device)
                },
                onDisconnect = { viewModel.disconnectBluetooth() },
                onRequestDiscoverable = {
                    val discoverIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                    }
                    discoverableLauncher.launch(discoverIntent)
                }
            )
        }
    }
}
