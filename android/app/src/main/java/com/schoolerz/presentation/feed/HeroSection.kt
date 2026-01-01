package com.schoolerz.presentation.feed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.schoolerz.domain.model.ServiceType

@Composable
fun HeroSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedService: ServiceType?,
    onServiceClick: (ServiceType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Headline
        Text(
            text = "Find trusted teen help",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "in your neighborhood",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar with clear button
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            placeholder = { Text("What do you need help with?") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Service Categories with All and More
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // "All" chip first
            item {
                AllCategoryChip(
                    isSelected = selectedService == null,
                    onClick = { onServiceClick(null) }
                )
            }

            // Service category chips
            items(ServiceType.entries.toList()) { service ->
                ServiceCategoryChip(
                    service = service,
                    icon = getIconForService(service),
                    isSelected = selectedService == service,
                    onClick = { onServiceClick(service) }
                )
            }

            // "More" chip last
            item {
                MoreCategoryChip(
                    onClick = {
                        Toast.makeText(context, "More categories coming soon!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun getIconForService(service: ServiceType): ImageVector {
    return when (service) {
        ServiceType.DOG_WALKING -> Icons.Default.Favorite // Paw-like
        ServiceType.TUTORING -> Icons.Default.Create // Book/pencil
        ServiceType.BABYSITTING -> Icons.Default.Face // Child face
        ServiceType.LAWN_CARE -> Icons.Default.Home // Home/garden
        ServiceType.TECH_HELP -> Icons.Default.Build // Tools
        ServiceType.MUSIC_LESSONS -> Icons.Default.PlayArrow // Music play
        ServiceType.ESSAY_REVIEW -> Icons.Default.Create // Writing
        ServiceType.CAR_WASH -> Icons.Default.Star // Car star
        ServiceType.OTHER -> Icons.Default.Info // Generic
    }
}
