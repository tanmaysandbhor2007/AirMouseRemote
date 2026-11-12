package com.airmouse.remote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airmouse.remote.model.TVKey
import com.airmouse.remote.ui.theme.AccentCyan
import com.airmouse.remote.ui.theme.BorderDark
import com.airmouse.remote.ui.theme.SurfaceDark
import com.airmouse.remote.ui.theme.SurfaceLightDark
import com.airmouse.remote.ui.theme.TextPrimary

@Composable
fun RemoteDPad(
    modifier: Modifier = Modifier,
    onKeyPress: (TVKey) -> Unit
) {
    val view = LocalView.current
    val playHaptic = {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(SurfaceDark)
            .border(2.dp, BorderDark, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // UP Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(76.dp)
                .offset(y = 8.dp)
                .clip(CircleShape)
                .clickable {
                    playHaptic()
                    onKeyPress(TVKey.UP)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = TextPrimary, modifier = Modifier.size(36.dp))
        }

        // DOWN Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(76.dp)
                .offset(y = (-8).dp)
                .clip(CircleShape)
                .clickable {
                    playHaptic()
                    onKeyPress(TVKey.DOWN)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = TextPrimary, modifier = Modifier.size(36.dp))
        }

        // LEFT Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(76.dp)
                .offset(x = 8.dp)
                .clip(CircleShape)
                .clickable {
                    playHaptic()
                    onKeyPress(TVKey.LEFT)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Left", tint = TextPrimary, modifier = Modifier.size(36.dp))
        }

        // RIGHT Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(76.dp)
                .offset(x = (-8).dp)
                .clip(CircleShape)
                .clickable {
                    playHaptic()
                    onKeyPress(TVKey.RIGHT)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Right", tint = TextPrimary, modifier = Modifier.size(36.dp))
        }

        // CENTER OK BUTTON
        Box(
            modifier = Modifier
                .size(86.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(SurfaceLightDark)
                .border(2.dp, AccentCyan.copy(alpha = 0.5f), CircleShape)
                .clickable {
                    playHaptic()
                    onKeyPress(TVKey.OK)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OK",
                color = AccentCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}
