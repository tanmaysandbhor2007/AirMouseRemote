package com.airmouse.remote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airmouse.remote.model.MouseButton
import com.airmouse.remote.ui.theme.AccentCyan
import com.airmouse.remote.ui.theme.BorderDark
import com.airmouse.remote.ui.theme.SurfaceDark
import com.airmouse.remote.ui.theme.SurfaceLightDark
import com.airmouse.remote.ui.theme.TextPrimary
import com.airmouse.remote.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun AirPointerPad(
    modifier: Modifier = Modifier,
    isAirPointerEnabled: Boolean,
    isHoldToAim: Boolean,
    onClutchStateChanged: (Boolean) -> Unit,
    onMouseButton: (MouseButton, Boolean) -> Unit,
    onScroll: (Int) -> Unit,
    onRecenter: () -> Unit
) {
    val view = LocalView.current
    val playHaptic = {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Clutch & Pointer Area with Scroll Strip on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Clutch / Aiming Touchpad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SurfaceDark, SurfaceLightDark.copy(alpha = 0.8f))
                        )
                    )
                    .border(
                        2.dp,
                        if (isAirPointerEnabled) AccentCyan.copy(alpha = 0.6f) else BorderDark,
                        RoundedCornerShape(24.dp)
                    )
                    .pointerInput(isAirPointerEnabled, isHoldToAim) {
                        detectTapGestures(
                            onPress = {
                                onClutchStateChanged(true)
                                playHaptic()
                                tryAwaitRelease()
                                onClutchStateChanged(false)
                            },
                            onTap = {
                                // Tap triggers quick left click
                                onMouseButton(MouseButton.LEFT, true)
                                playHaptic()
                                onMouseButton(MouseButton.LEFT, false)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(if (isAirPointerEnabled) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                if (isAirPointerEnabled) AccentCyan.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .border(
                                2.dp,
                                if (isAirPointerEnabled) AccentCyan else TextSecondary.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isAirPointerEnabled) Icons.Default.CenterFocusStrong else Icons.Default.TouchApp,
                            contentDescription = "Pointer Indicator",
                            tint = if (isAirPointerEnabled) AccentCyan else TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (!isAirPointerEnabled) "Air Pointer Paused"
                        else if (isHoldToAim) "Hold Thumb Here to Aim"
                        else "Air Pointer Active (Move Phone)",
                        color = if (isAirPointerEnabled) TextPrimary else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap surface to Left Click",
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Right Vertical Scroll Bar
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.5.dp, BorderDark, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        var accumulatedDelta = 0f
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDelta += dragAmount.y
                                if (kotlin.math.abs(accumulatedDelta) > 15f) {
                                    val scrollStep = (-accumulatedDelta / 15f).roundToInt()
                                    onScroll(scrollStep)
                                    accumulatedDelta = 0f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.UnfoldMore,
                        contentDescription = "Scroll Strip",
                        tint = AccentCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SCROLL",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mouse Action Buttons (Left Click & Right Click) + Recenter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Large Primary Left Click Button
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1.8f)
                    .height(64.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                playHaptic()
                                onMouseButton(MouseButton.LEFT, true)
                                tryAwaitRelease()
                                onMouseButton(MouseButton.LEFT, false)
                            }
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = SurfaceDark
                )
            ) {
                Text(
                    text = "LEFT CLICK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            // Right Click Button
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1.2f)
                    .height(64.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                playHaptic()
                                onMouseButton(MouseButton.RIGHT, true)
                                tryAwaitRelease()
                                onMouseButton(MouseButton.RIGHT, false)
                            }
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceLightDark,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "RIGHT",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
