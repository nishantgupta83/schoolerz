package com.schoolerz.presentation.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.schoolerz.domain.model.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCategoryChip(
    service: ServiceType,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = "${service.displayName} service category" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilterChip(
            selected = isSelected,
            onClick = onClick,
            label = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = service.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Special "All" chip for clearing category filter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCategoryChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = "All categories" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilterChip(
            selected = isSelected,
            onClick = onClick,
            label = {
                Text(
                    text = "All",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.height(56.dp)
        )
    }
}

// "More" chip for future categories
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreCategoryChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = "More categories" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilterChip(
            selected = false,
            onClick = onClick,
            label = {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.height(56.dp)
        )
    }
}
