package com.airmouse.remote.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airmouse.remote.model.ConnectionState
import com.airmouse.remote.ui.theme.AccentCyan
import com.airmouse.remote.ui.theme.ActiveGreen
import com.airmouse.remote.ui.theme.BorderDark
import com.airmouse.remote.ui.theme.ErrorRed
import com.airmouse.remote.ui.theme.SurfaceDark
import com.airmouse.remote.ui.theme.SurfaceLightDark
import com.airmouse.remote.ui.theme.TextPrimary
import com.airmouse.remote.ui.theme.TextSecondary
import com.airmouse.remote.ui.theme.WarningAmber

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceDialog(
    connectionState: ConnectionState,
    bondedDevices: List<BluetoothDevice>,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit,
    onRequestDiscoverable: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bluetooth Devices",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AccentCyan
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connection status banner
                val (statusBg, statusText) = when (connectionState) {
                    is ConnectionState.Connected -> Pair(ActiveGreen.copy(alpha = 0.15f), "Connected to ${connectionState.targetName}")
                    is ConnectionState.Connecting -> Pair(WarningAmber.copy(alpha = 0.15f), "Connecting...")
                    is ConnectionState.Disconnected -> Pair(SurfaceLightDark, "Not connected to any device")
                    is ConnectionState.Error -> Pair(ErrorRed.copy(alpha = 0.15f), connectionState.message)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (connectionState is ConnectionState.Connected) {
                            Button(
                                onClick = onDisconnect,
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Disconnect", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Quick Action: Make phone discoverable & Open BT Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestDiscoverable,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = SurfaceDark)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pair New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BT Settings", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Text(
                    text = "Paired Devices (Tap to connect):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                if (bondedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No paired devices found.\nTap 'Pair New' to make this phone discoverable, then pair from your PC or TV.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bondedDevices) { device ->
                            val isCurrent = (connectionState as? ConnectionState.Connected)?.targetName == (device.name ?: device.address)
                            val icon = getDeviceIcon(device)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) AccentCyan.copy(alpha = 0.1f) else SurfaceLightDark)
                                    .border(
                                        1.dp,
                                        if (isCurrent) AccentCyan else BorderDark,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onConnectDevice(device)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isCurrent) AccentCyan else TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name ?: "Unknown Device",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = device.address,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (isCurrent) {
                                    Text(
                                        text = "CONNECTED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ActiveGreen
                                    )
                                } else {
                                    Text(
                                        text = "CONNECT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark, contentColor = TextPrimary)
            ) {
                Text("Close")
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}

@SuppressLint("MissingPermission")
private fun getDeviceIcon(device: BluetoothDevice): ImageVector {
    val devClass = device.bluetoothClass ?: return Icons.Default.Devices
    val major = devClass.majorDeviceClass

    return when (major) {
        BluetoothClass.Device.Major.COMPUTER -> Icons.Default.Computer
        BluetoothClass.Device.Major.AUDIO_VIDEO -> {
            val devSub = devClass.deviceClass
            if (devSub == BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX ||
                devSub == BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER) {
                Icons.Default.Tv
            } else {
                Icons.Default.Headphones
            }
        }
        else -> Icons.Default.Bluetooth
    }
}
