package com.airmouse.remote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.airmouse.remote.model.TVKey
import com.airmouse.remote.ui.theme.BorderDark
import com.airmouse.remote.ui.theme.SurfaceDark
import com.airmouse.remote.ui.theme.TextPrimary

@Composable
fun TVControlsRow(
    modifier: Modifier = Modifier,
    onKeyPress: (TVKey) -> Unit
) {
    val view = LocalView.current
    val playHaptic = {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Navigation auxiliary row: Back, Home, Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                description = "Back"
            ) {
                playHaptic()
                onKeyPress(TVKey.BACK)
            }

            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Home,
                description = "Home"
            ) {
                playHaptic()
                onKeyPress(TVKey.HOME)
            }

            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Menu,
                description = "Menu"
            ) {
                playHaptic()
                onKeyPress(TVKey.MENU)
            }
        }

        // Media & Volume row: Vol -, Mute, Vol +, Play/Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.VolumeDown,
                description = "Volume Down"
            ) {
                playHaptic()
                onKeyPress(TVKey.VOLUME_DOWN)
            }

            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.VolumeMute,
                description = "Mute"
            ) {
                playHaptic()
                onKeyPress(TVKey.MUTE)
            }

            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                description = "Volume Up"
            ) {
                playHaptic()
                onKeyPress(TVKey.VOLUME_UP)
            }

            TVIconButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PlayArrow,
                description = "Play/Pause"
            ) {
                playHaptic()
                onKeyPress(TVKey.PLAY_PAUSE)
            }
        }
    }
}

@Composable
private fun TVIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.5.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}
