package com.airmouse.remote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airmouse.remote.model.ConnectionState
import com.airmouse.remote.model.TransportType
import com.airmouse.remote.ui.theme.AccentCyan
import com.airmouse.remote.ui.theme.ActiveGreen
import com.airmouse.remote.ui.theme.BorderDark
import com.airmouse.remote.ui.theme.ErrorRed
import com.airmouse.remote.ui.theme.SurfaceDark
import com.airmouse.remote.ui.theme.TextPrimary
import com.airmouse.remote.ui.theme.TextSecondary
import com.airmouse.remote.ui.theme.WarningAmber

@Composable
fun ConnectionHeader(
    modifier: Modifier = Modifier,
    transportType: TransportType,
    connectionState: ConnectionState,
    isAirPointerEnabled: Boolean,
    onToggleTransport: () -> Unit,
    onToggleAirPointer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val view = LocalView.current
    val playHaptic = {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    val airPointerBgColor by animateColorAsState(
        targetValue = if (isAirPointerEnabled) AccentCyan else SurfaceDark,
        animationSpec = tween(300),
        label = "airPointerBg"
    )

    val airPointerContentColor by animateColorAsState(
        targetValue = if (isAirPointerEnabled) SurfaceDark else TextPrimary,
        animationSpec = tween(300),
        label = "airPointerText"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Transport and Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Transport Switcher Pill (Bluetooth <-> Wi-Fi)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                    .clickable {
                        playHaptic()
                        onToggleTransport()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (transportType == TransportType.BLUETOOTH) Icons.Default.Bluetooth else Icons.Default.Wifi,
                    contentDescription = "Transport",
                    tint = AccentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (transportType == TransportType.BLUETOOTH) "Bluetooth HID" else "Wi-Fi (UDP)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            // Connection State Indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val (statusColor, statusText) = when (connectionState) {
                    is ConnectionState.Connected -> Pair(ActiveGreen, connectionState.targetName)
                    is ConnectionState.Connecting -> Pair(WarningAmber, "Connecting...")
                    is ConnectionState.Disconnected -> Pair(TextSecondary, "Pairing Ready")
                    is ConnectionState.Error -> Pair(ErrorRed, "Error")
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            // Settings Button
            IconButton(
                onClick = {
                    playHaptic()
                    onOpenSettings()
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, CircleShape)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // PRIMARY AIR POINTER TOGGLE BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(airPointerBgColor)
                .border(
                    2.dp,
                    if (isAirPointerEnabled) AccentCyan else BorderDark,
                    RoundedCornerShape(16.dp)
                )
                .clickable {
                    playHaptic()
                    onToggleAirPointer()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAirPointerEnabled) Icons.Default.GpsFixed else Icons.Default.Navigation,
                    contentDescription = "Air Pointer Toggle",
                    tint = airPointerContentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isAirPointerEnabled) "AIR POINTER ACTIVE (GYRO ON)" else "TAP TO ACTIVATE AIR POINTER",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = airPointerContentColor,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
