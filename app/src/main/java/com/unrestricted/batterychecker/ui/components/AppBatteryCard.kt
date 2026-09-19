package com.unrestricted.batterychecker.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unrestricted.batterychecker.model.AppBatteryInfo
import com.unrestricted.batterychecker.model.BatteryOptimizationState
import com.unrestricted.batterychecker.ui.theme.ColorOptimized
import com.unrestricted.batterychecker.ui.theme.ColorRestricted
import com.unrestricted.batterychecker.ui.theme.ColorUnknown
import com.unrestricted.batterychecker.ui.theme.ColorUnrestricted

@Composable
fun AppBatteryCard(
    app: AppBatteryInfo,
    shizukuReady: Boolean,
    onStateChange: (BatteryOptimizationState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val stateColor by animateColorAsState(
        targetValue = when (app.state) {
            BatteryOptimizationState.UNRESTRICTED -> ColorUnrestricted
            BatteryOptimizationState.OPTIMIZED -> ColorOptimized
            BatteryOptimizationState.RESTRICTED -> ColorRestricted
            BatteryOptimizationState.UNKNOWN -> ColorUnknown
        },
        label = "stateColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Real App Icon with small State indicator badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.size(46.dp)) {
                        AppIcon(
                            packageName = app.packageName,
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.Center)
                        )
                        // Tiny dot indicator on the corner of the icon
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(stateColor, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Open System Settings
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(fallbackIntent)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = "Open System Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // State Badge & Meta info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = stateColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(stateColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = app.state.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = stateColor
                        )
                    }
                }

                if (app.isSystemApp) {
                    Text(
                        text = "System App",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Quick State Toggle Buttons (when Shizuku is active)
            if (shizukuReady) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatePill(
                        text = "Unrestrict",
                        isSelected = app.state == BatteryOptimizationState.UNRESTRICTED,
                        activeColor = ColorUnrestricted,
                        onClick = { onStateChange(BatteryOptimizationState.UNRESTRICTED) },
                        modifier = Modifier.weight(1f)
                    )
                    StatePill(
                        text = "Optimize",
                        isSelected = app.state == BatteryOptimizationState.OPTIMIZED,
                        activeColor = ColorOptimized,
                        onClick = { onStateChange(BatteryOptimizationState.OPTIMIZED) },
                        modifier = Modifier.weight(1f)
                    )
                    StatePill(
                        text = "Restrict",
                        isSelected = app.state == BatteryOptimizationState.RESTRICTED,
                        activeColor = ColorRestricted,
                        onClick = { onStateChange(BatteryOptimizationState.RESTRICTED) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatePill(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(32.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        )
    }
}
