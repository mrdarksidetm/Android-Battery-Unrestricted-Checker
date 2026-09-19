package com.unrestricted.batterychecker.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unrestricted.batterychecker.model.FilterMode
import com.unrestricted.batterychecker.model.OptimizationStats

@Composable
fun ExpressiveFilterBar(
    selectedFilter: FilterMode,
    stats: OptimizationStats,
    onFilterSelected: (FilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterMode.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val countText = when (filter) {
                FilterMode.ALL -> "(${stats.totalApps})"
                FilterMode.UNRESTRICTED -> "(${stats.unrestrictedCount})"
                FilterMode.OPTIMIZED -> "(${stats.optimizedCount})"
                FilterMode.RESTRICTED -> "(${stats.restrictedCount})"
                FilterMode.USER_ONLY -> ""
                FilterMode.SYSTEM_ONLY -> ""
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(16.dp),
                label = {
                    Text(
                        text = "${filter.label} $countText".trim(),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
