package com.schoolerz.presentation.feed

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolerz.domain.model.ExperienceLevel
import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.model.RateType
import com.schoolerz.domain.model.ServiceType
import com.schoolerz.presentation.theme.Tokens
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showContactSheet by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }

    // Generate shareable content for the post
    fun getShareContent(): String {
        val typeLabel = if (post.type == PostType.OFFER) "Service Offered" else "Service Needed"
        val content = StringBuilder().apply {
            appendLine("$typeLabel on Schoolerz")
            appendLine()
            appendLine("${post.authorName} - ${post.neighborhood}")
            appendLine()
            appendLine(post.body)
            appendLine()
            appendLine("Rate: ${post.formattedPrice}")
            post.formattedAvailability?.let {
                appendLine("Availability: $it")
            }
            if (post.skillTags.isNotEmpty()) {
                appendLine("Skills: ${post.skillTags.joinToString(", ")}")
            }
            appendLine()
            appendLine("View on Schoolerz: https://schoolerz.app/post/${post.id}")
        }
        return content.toString()
    }

    fun sharePost() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareContent())
            putExtra(Intent.EXTRA_SUBJECT, "Check out this post on Schoolerz")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showReportSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Author Header
            AuthorHeader(post = post)

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            // Post Type Badge
            PostTypeBadge(postType = post.type)

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            // Pricing Card
            if (post.rateType != RateType.NEGOTIABLE || post.rateAmount != null) {
                PricingCard(post = post)
                Spacer(modifier = Modifier.height(Tokens.Spacing.m))
            }

            // Full Post Body
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = Tokens.Spacing.m)
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            // Service Type
            post.serviceType?.let { serviceType ->
                ServiceTypeRow(serviceType = serviceType)
                Spacer(modifier = Modifier.height(Tokens.Spacing.m))
            }

            // Skills Tags
            if (post.skillTags.isNotEmpty()) {
                SkillTagsSection(skillTags = post.skillTags)
                Spacer(modifier = Modifier.height(Tokens.Spacing.m))
            }

            // Availability
            post.formattedAvailability?.let { availability ->
                AvailabilitySection(availability = availability)
                Spacer(modifier = Modifier.height(Tokens.Spacing.m))
            }

            Divider(modifier = Modifier.padding(horizontal = Tokens.Spacing.m))

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            // Engagement Stats
            EngagementStats(post = post, onShare = { sharePost() })

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            // Action Buttons
            ActionButtons(
                onContact = { showContactSheet = true },
                onSave = { /* Save to shortlist */ }
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            Divider(modifier = Modifier.padding(horizontal = Tokens.Spacing.m))

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            // Comments Section Header
            CommentsHeader(commentCount = post.commentCount)

            Spacer(modifier = Modifier.height(Tokens.Spacing.xl))
        }
    }

    // Contact Sheet
    if (showContactSheet) {
        ContactSheet(
            authorName = post.authorName,
            onDismiss = { showContactSheet = false },
            onSend = {
                // Send contact request
                showContactSheet = false
            }
        )
    }

    // Report Sheet
    if (showReportSheet) {
        ReportSheet(
            onDismiss = { showReportSheet = false },
            onReport = {
                // Submit report
                showReportSheet = false
            }
        )
    }
}

@Composable
private fun AuthorHeader(post: Post) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Tokens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (post.type == PostType.OFFER)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = post.authorInitials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (post.type == PostType.OFFER)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(Tokens.Spacing.m))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                post.experienceLevel?.let { level ->
                    Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                    ExperienceBadge(level = level)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.neighborhood,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = getTimeAgo(post.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ExperienceBadge(level: ExperienceLevel) {
    val badgeColor = when (level) {
        ExperienceLevel.BEGINNER -> Color(0xFF9CA3AF)
        ExperienceLevel.INTERMEDIATE -> Color(0xFF3B82F6)
        ExperienceLevel.EXPERIENCED -> Color(0xFF10B981)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.2f)
    ) {
        Text(
            text = level.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PostTypeBadge(postType: PostType) {
    val isOffer = postType == PostType.OFFER
    val color = if (isOffer)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(horizontal = Tokens.Spacing.m)
    ) {
        Text(
            text = if (isOffer) "OFFERING" else "LOOKING FOR",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PricingCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Tokens.Spacing.m),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(Tokens.Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(Tokens.Spacing.s))

            Column {
                Text(
                    text = "Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = post.formattedPrice,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ServiceTypeRow(serviceType: ServiceType) {
    Row(
        modifier = Modifier.padding(horizontal = Tokens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (serviceType) {
                ServiceType.DOG_WALKING -> Icons.Default.Pets
                ServiceType.TUTORING -> Icons.Default.School
                ServiceType.BABYSITTING -> Icons.Default.ChildCare
                ServiceType.LAWN_CARE -> Icons.Default.Grass
                ServiceType.TECH_HELP -> Icons.Default.Laptop
                ServiceType.MUSIC_LESSONS -> Icons.Default.MusicNote
                ServiceType.ESSAY_REVIEW -> Icons.Default.Edit
                ServiceType.CAR_WASH -> Icons.Default.LocalCarWash
                ServiceType.OTHER -> Icons.Default.MoreHoriz
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(Tokens.Spacing.s))

        Text(
            text = serviceType.displayName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SkillTagsSection(skillTags: List<String>) {
    Column(modifier = Modifier.padding(horizontal = Tokens.Spacing.m)) {
        Text(
            text = "Skills & Certifications",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(Tokens.Spacing.s))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)
        ) {
            items(skillTags.take(5)) { tag ->
                SuggestionChip(
                    onClick = { },
                    label = { Text(tag) }
                )
            }
        }
    }
}

@Composable
private fun AvailabilitySection(availability: String) {
    Column(modifier = Modifier.padding(horizontal = Tokens.Spacing.m)) {
        Text(
            text = "Availability",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(Tokens.Spacing.s))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(Tokens.Spacing.s))

            Text(
                text = availability,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EngagementStats(post: Post, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Tokens.Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${post.likeCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(Tokens.Spacing.l))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${post.commentCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onShare) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onContact: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Tokens.Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.m)
    ) {
        Button(
            onClick = onContact,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Message, contentDescription = null)
            Spacer(modifier = Modifier.width(Tokens.Spacing.s))
            Text("Contact")
        }

        OutlinedButton(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(Tokens.Spacing.s))
            Text("Save")
        }
    }
}

@Composable
private fun CommentsHeader(commentCount: Int) {
    Column(modifier = Modifier.padding(horizontal = Tokens.Spacing.m)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(Tokens.Spacing.s))

            Text(
                text = "($commentCount)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        Text(
            text = "Comments will be loaded here...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactSheet(
    authorName: String,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Tokens.Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            Text(
                text = "Contact Request",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.s))

            Text(
                text = "Your request to connect with $authorName will be sent to their parent/guardian for approval.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Tokens.Spacing.m)
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.xl))

            Button(
                onClick = onSend,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Request")
            }

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(Tokens.Spacing.l))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSheet(
    onDismiss: () -> Unit,
    onReport: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Tokens.Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            Text(
                text = "Report Post",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.s))

            Text(
                text = "If this post contains inappropriate content, please let us know.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Tokens.Spacing.m)
            )

            Spacer(modifier = Modifier.height(Tokens.Spacing.xl))

            Button(
                onClick = onReport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Report")
            }

            Spacer(modifier = Modifier.height(Tokens.Spacing.m))

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(Tokens.Spacing.l))
        }
    }
}

private fun getTimeAgo(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "just now"
    }
}
