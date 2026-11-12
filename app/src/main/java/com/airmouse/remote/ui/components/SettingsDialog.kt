package com.airmouse.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airmouse.remote.model.AirPointerSettings
import com.airmouse.remote.ui.theme.AccentCyan
import com.airmouse.remote.ui.theme.BorderDark
import com.airmouse.remote.ui.theme.SurfaceDark
import com.airmouse.remote.ui.theme.SurfaceLightDark
import com.airmouse.remote.ui.theme.TextPrimary
import com.airmouse.remote.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SettingsDialog(
    currentSettings: AirPointerSettings,
    onDismiss: () -> Unit,
    onSave: (AirPointerSettings) -> Unit
) {
    var sensitivity by remember { mutableFloatStateOf(currentSettings.sensitivity) }
    var deadzone by remember { mutableFloatStateOf(currentSettings.deadzone) }
    var invertX by remember { mutableStateOf(currentSettings.invertX) }
    var invertY by remember { mutableStateOf(currentSettings.invertY) }
    var holdToAim by remember { mutableStateOf(currentSettings.holdToAim) }
    var hostIp by remember { mutableStateOf(currentSettings.wifiHostIp) }
    var port by remember { mutableStateOf(currentSettings.wifiPort.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Control & Sensor Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SENSITIVITY
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gyro Sensitivity", color = TextPrimary, fontSize = 13.sp)
                        Text(String.format(Locale.US, "%.1fx", sensitivity), color = AccentCyan, fontSize = 13.sp)
                    }
                    Slider(
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )
                }

                // DEADZONE
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tremor Filter (Deadzone)", color = TextPrimary, fontSize = 13.sp)
                        Text(String.format(Locale.US, "%.3f", deadzone), color = AccentCyan, fontSize = 13.sp)
                    }
                    Slider(
                        value = deadzone,
                        onValueChange = { deadzone = it },
                        valueRange = 0.005f..0.08f,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )
                }

                // HOLD TO AIM TOGGLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hold Thumb to Aim", color = TextPrimary, fontSize = 13.sp)
                        Text(
                            "Aim only while touching screen",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = holdToAim,
                        onCheckedChange = { holdToAim = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = SurfaceLightDark)
                    )
                }

                // INVERT X / Y
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Invert X", color = TextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = invertX,
                            onCheckedChange = { invertX = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Invert Y", color = TextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = invertY,
                            onCheckedChange = { invertY = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan)
                        )
                    }
                }

                // WI-FI SETTINGS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLightDark, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Wi-Fi Companion Host", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = hostIp,
                        onValueChange = { hostIp = it },
                        label = { Text("PC / TV IP Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("UDP Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPort = port.toIntOrNull() ?: 8888
                    onSave(
                        currentSettings.copy(
                            sensitivity = sensitivity,
                            deadzone = deadzone,
                            invertX = invertX,
                            invertY = invertY,
                            holdToAim = holdToAim,
                            wifiHostIp = hostIp.trim(),
                            wifiPort = parsedPort
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = SurfaceDark)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark, contentColor = TextPrimary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}
