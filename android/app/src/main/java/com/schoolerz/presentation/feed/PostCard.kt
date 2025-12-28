package com.schoolerz.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.presentation.theme.Colors
import com.schoolerz.presentation.theme.Tokens
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostCard(post: Post, onComment: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Tokens.Radius.medium)
    ) {
        Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Colors.Seed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.authorInitials, color = Colors.Seed, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(Tokens.Spacing.s))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${post.neighborhood} • ${formatTimeAgo(post.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TypeBadge(post.type)
            }
            Spacer(Modifier.height(Tokens.Spacing.s))
            Text(post.body, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
            Spacer(Modifier.height(Tokens.Spacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.l)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${post.likeCount}", style = MaterialTheme.typography.labelMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onComment() }
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${post.commentCount}", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TypeBadge(type: PostType) {
    val color = if (type == PostType.OFFER) Colors.Offer else Colors.Request
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = if (type == PostType.OFFER) "Offer" else "Request",
            modifier = Modifier.padding(horizontal = Tokens.Spacing.s, vertical = Tokens.Spacing.xs),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatTimeAgo(date: Date): String {
    val diff = System.currentTimeMillis() - date.time
    return when {
        diff < 60_000 -> "now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
