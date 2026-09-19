package com.unrestricted.batterychecker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val total = if (stats.totalApps > 0) stats.totalApps.toFloat() else 1f

    val unrestrictedFraction = stats.unrestrictedCount / total
    val optimizedFraction = stats.optimizedCount / total
    val restrictedFraction = stats.restrictedCount / total
    val unknownFraction = stats.unknownCount / total

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Battery Mode Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${stats.totalApps} Apps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Native Canvas Custom Multi-segment Distribution Bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            ) {
                val barWidth = size.width
                val barHeight = size.height
                val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

                // Background track
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = cornerRadius
                )

                var currentX = 0f
                val currentProgress = animatedProgress.value

                // 1. Unrestricted segment (Jade Green)
                val wUnrestricted = barWidth * unrestrictedFraction * currentProgress
                if (wUnrestricted > 0f) {
                    drawRoundRect(
                        color = ColorUnrestricted,
                        topLeft = Offset(currentX, 0f),
                        size = Size(wUnrestricted, barHeight),
                        cornerRadius = cornerRadius
                    )
                    currentX += wUnrestricted
                }

                // 2. Optimized segment (Dodger Blue)
                val wOptimized = barWidth * optimizedFraction * currentProgress
                if (wOptimized > 0f) {
                    drawRoundRect(
                        color = ColorOptimized,
                        topLeft = Offset(currentX, 0f),
                        size = Size(wOptimized, barHeight),
                        cornerRadius = cornerRadius
                    )
                    currentX += wOptimized
                }

                // 3. Restricted segment (Crimson Red)
                val wRestricted = barWidth * restrictedFraction * currentProgress
                if (wRestricted > 0f) {
                    drawRoundRect(
                        color = ColorRestricted,
                        topLeft = Offset(currentX, 0f),
                        size = Size(wRestricted, barHeight),
                        cornerRadius = cornerRadius
                    )
                    currentX += wRestricted
                }

                // 4. Unknown / Not Whitelisted segment (Slate Grey)
                val wUnknown = barWidth * unknownFraction * currentProgress
                if (wUnknown > 0f) {
                    drawRoundRect(
                        color = ColorUnknown,
                        topLeft = Offset(currentX, 0f),
                        size = Size(wUnknown, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend indicators with counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = ColorUnrestricted, label = "Unrestricted", count = stats.unrestrictedCount)
                LegendItem(color = ColorOptimized, label = "Optimized", count = stats.optimizedCount)
                LegendItem(color = ColorRestricted, label = "Restricted", count = stats.restrictedCount)
                if (stats.unknownCount > 0) {
                    LegendItem(color = ColorUnknown, label = "Default", count = stats.unknownCount)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(8.dp)
        ) {}
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
