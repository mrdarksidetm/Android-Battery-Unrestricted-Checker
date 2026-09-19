package com.unrestricted.batterychecker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unrestricted.batterychecker.model.OptimizationStats
import com.unrestricted.batterychecker.ui.theme.ColorOptimized
import com.unrestricted.batterychecker.ui.theme.ColorRestricted
import com.unrestricted.batterychecker.ui.theme.ColorUnknown
import com.unrestricted.batterychecker.ui.theme.ColorUnrestricted

@Composable
fun BatteryDistributionCanvas(
    stats: OptimizationStats,
    modifier: Modifier = Modifier
) {
    val totalInt = stats.totalApps
    val total = if (totalInt > 0) totalInt.toFloat() else 1f

    val unrestrictedFraction = stats.unrestrictedCount / total
    val optimizedFraction = stats.optimizedCount / total
    val restrictedFraction = stats.restrictedCount / total
    val unknownFraction = stats.unknownCount / total

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Title & Total Apps Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Battery Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "$totalInt Apps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expressive 3-Segment Metric Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(
                    label = "Unrestricted",
                    count = stats.unrestrictedCount,
                    percent = if (totalInt > 0) (stats.unrestrictedCount * 100 / totalInt) else 0,
                    icon = Icons.Rounded.Bolt,
                    color = ColorUnrestricted,
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    label = "Optimized",
                    count = stats.optimizedCount,
                    percent = if (totalInt > 0) (stats.optimizedCount * 100 / totalInt) else 0,
                    icon = Icons.Rounded.BatterySaver,
                    color = ColorOptimized,
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    label = "Restricted",
                    count = stats.restrictedCount,
                    percent = if (totalInt > 0) (stats.restrictedCount * 100 / totalInt) else 0,
                    icon = Icons.Rounded.BatteryAlert,
                    color = ColorRestricted,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expressive Multi-segment Canvas Bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                val barWidth = size.width
                val barHeight = size.height
                val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

                // Background track
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = cornerRadius
                )

                var currentX = 0f
                val currentProgress = animatedProgress.value
                val gap = 2.dp.toPx()

                // 1. Unrestricted segment (Emerald Green)
                val wUnrestricted = (barWidth * unrestrictedFraction * currentProgress).coerceAtLeast(0f)
                if (wUnrestricted > 0f) {
                    val drawWidth = (wUnrestricted - gap).coerceAtLeast(barHeight)
                    drawRoundRect(
                        color = ColorUnrestricted,
                        topLeft = Offset(currentX, 0f),
                        size = Size(drawWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                    currentX += wUnrestricted
                }

                // 2. Optimized segment (Dodger Blue)
                val wOptimized = (barWidth * optimizedFraction * currentProgress).coerceAtLeast(0f)
                if (wOptimized > 0f) {
                    val drawWidth = (wOptimized - gap).coerceAtLeast(barHeight)
                    drawRoundRect(
                        color = ColorOptimized,
                        topLeft = Offset(currentX, 0f),
                        size = Size(drawWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                    currentX += wOptimized
                }

                // 3. Restricted segment (Crimson Red)
                val wRestricted = (barWidth * restrictedFraction * currentProgress).coerceAtLeast(0f)
                if (wRestricted > 0f) {
                    val drawWidth = (wRestricted - gap).coerceAtLeast(barHeight)
                    drawRoundRect(
                        color = ColorRestricted,
                        topLeft = Offset(currentX, 0f),
                        size = Size(drawWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                    currentX += wRestricted
                }

                // 4. Unknown / Default segment (Slate)
                val wUnknown = (barWidth * unknownFraction * currentProgress).coerceAtLeast(0f)
                if (wUnknown > 0f) {
                    drawRoundRect(
                        color = ColorUnknown,
                        topLeft = Offset(currentX, 0f),
                        size = Size(wUnknown, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    count: Int,
    percent: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
