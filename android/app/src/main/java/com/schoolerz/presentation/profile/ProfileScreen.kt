package com.schoolerz.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus
import com.schoolerz.presentation.theme.Colors
import com.schoolerz.presentation.theme.Tokens
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    if (state.profile != null && !state.isEditing) {
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    IconButton(onClick = { viewModel.refreshProfile() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.profile != null -> {
                    ProfileContent(
                        profile = state.profile!!,
                        isEditing = state.isEditing,
                        isSaving = state.isSaving,
                        editingDisplayName = state.editingDisplayName,
                        editingSchoolName = state.editingSchoolName,
                        editingGrade = state.editingGrade,
                        editingBio = state.editingBio,
                        editingNeighborhood = state.editingNeighborhood,
                        editingServices = state.editingServices,
                        onDisplayNameChange = viewModel::updateDisplayName,
                        onSchoolNameChange = viewModel::updateSchoolName,
                        onGradeChange = viewModel::updateGrade,
                        onBioChange = viewModel::updateBio,
                        onNeighborhoodChange = viewModel::updateNeighborhood,
                        onServiceToggle = viewModel::toggleService,
                        onSave = viewModel::saveProfile,
                        onCancel = viewModel::cancelEditing
                    )
                }
                else -> {
                    SetupProfileContent(
                        isSaving = state.isSaving,
                        displayName = state.editingDisplayName,
                        schoolName = state.editingSchoolName,
                        grade = state.editingGrade,
                        bio = state.editingBio,
                        neighborhood = state.editingNeighborhood,
                        selectedServices = state.editingServices,
                        onDisplayNameChange = viewModel::updateDisplayName,
                        onSchoolNameChange = viewModel::updateSchoolName,
                        onGradeChange = viewModel::updateGrade,
                        onBioChange = viewModel::updateBio,
                        onNeighborhoodChange = viewModel::updateNeighborhood,
                        onServiceToggle = viewModel::toggleService,
                        onSave = viewModel::saveProfile
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    profile: Profile,
    isEditing: Boolean,
    isSaving: Boolean,
    editingDisplayName: String,
    editingSchoolName: String,
    editingGrade: String,
    editingBio: String,
    editingNeighborhood: String,
    editingServices: Set<String>,
    onDisplayNameChange: (String) -> Unit,
    onSchoolNameChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onServiceToggle: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Tokens.Spacing.m)
    ) {
        // Avatar and Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(Tokens.Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with gradient
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Colors.Seed,
                                    Colors.Seed.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initials,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                // Display Name
                AnimatedVisibility(
                    visible = isEditing,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    OutlinedTextField(
                        value = editingDisplayName,
                        onValueChange = onDisplayNameChange,
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(
                    visible = !isEditing,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.xs))

                // Verification Badge
                VerificationBadge(status = profile.verificationStatus)
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        // School Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Colors.Seed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                    Text(
                        text = "School Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                if (isEditing) {
                    OutlinedTextField(
                        value = editingSchoolName,
                        onValueChange = onSchoolNameChange,
                        label = { Text("School Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        }
                    )

                    Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                    OutlinedTextField(
                        value = editingGrade,
                        onValueChange = onGradeChange,
                        label = { Text("Grade") },
                        placeholder = { Text("e.g., 11th Grade") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Grade, contentDescription = null)
                        }
                    )
                } else {
                    InfoRow(
                        label = "School",
                        value = profile.schoolName ?: "Not set",
                        isEmpty = profile.schoolName == null
                    )
                    Spacer(modifier = Modifier.height(Tokens.Spacing.s))
                    InfoRow(
                        label = "Grade",
                        value = profile.grade ?: "Not set",
                        isEmpty = profile.grade == null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        // Services Offered Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkOutline,
                        contentDescription = null,
                        tint = Colors.Seed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                    Text(
                        text = "Services Offered",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                if (isEditing) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s),
                        verticalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)
                    ) {
                        Profile.AVAILABLE_SERVICES.forEach { service ->
                            FilterChip(
                                selected = editingServices.contains(service),
                                onClick = { onServiceToggle(service) },
                                label = { Text(service) },
                                leadingIcon = if (editingServices.contains(service)) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                } else {
                    if (profile.services.isEmpty()) {
                        Text(
                            text = "No services selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s),
                            verticalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)
                        ) {
                            profile.services.forEach { service ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(service) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        // About Me Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Colors.Seed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                    Text(
                        text = "About Me",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                if (isEditing) {
                    OutlinedTextField(
                        value = editingBio,
                        onValueChange = onBioChange,
                        label = { Text("Bio") },
                        placeholder = { Text("Tell neighbors about yourself...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                } else {
                    Text(
                        text = profile.bio ?: "No bio yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (profile.bio == null)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        // Location & Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Colors.Seed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                    Text(
                        text = "Location & Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                if (isEditing) {
                    OutlinedTextField(
                        value = editingNeighborhood,
                        onValueChange = onNeighborhoodChange,
                        label = { Text("Neighborhood") },
                        placeholder = { Text("e.g., Westside") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        }
                    )
                } else {
                    InfoRow(
                        label = "Neighborhood",
                        value = profile.neighborhood ?: "Not set",
                        isEmpty = profile.neighborhood == null
                    )
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.s))
                Divider(modifier = Modifier.padding(vertical = Tokens.Spacing.s))
                Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                InfoRow(
                    label = "Member Since",
                    value = formatDate(profile.createdAt.time),
                    isEmpty = false
                )
            }
        }

        // Edit Actions
        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(Tokens.Spacing.l))

                FilledTonalButton(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && editingDisplayName.isNotBlank(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                        Text("Saving...")
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                        Text("Save Changes")
                    }
                }

                Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                    Text("Cancel")
                }
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.xl))
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SetupProfileContent(
    isSaving: Boolean,
    displayName: String,
    schoolName: String,
    grade: String,
    bio: String,
    neighborhood: String,
    selectedServices: Set<String>,
    onDisplayNameChange: (String) -> Unit,
    onSchoolNameChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onServiceToggle: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Tokens.Spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Tokens.Spacing.l))

        // Welcome Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Colors.Seed,
                            Colors.Seed.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.l))

        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Tokens.Spacing.s))

        Text(
            text = "Join the Schoolerz community and start connecting with neighbors",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Tokens.Spacing.xl))

        // Basic Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("Display Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = onSchoolNameChange,
                    label = { Text("School Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                OutlinedTextField(
                    value = grade,
                    onValueChange = onGradeChange,
                    label = { Text("Grade") },
                    placeholder = { Text("e.g., 11th Grade") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Grade, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = onNeighborhoodChange,
                    label = { Text("Neighborhood") },
                    placeholder = { Text("e.g., Westside") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        // Services Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Text(
                    text = "Services You Offer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.s))

                Text(
                    text = "Select the services you'd like to offer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)
                ) {
                    Profile.AVAILABLE_SERVICES.forEach { service ->
                        FilterChip(
                            selected = selectedServices.contains(service),
                            onClick = { onServiceToggle(service) },
                            label = { Text(service) },
                            leadingIcon = if (selectedServices.contains(service)) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.m))

        // Bio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
                Text(
                    text = "About You",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Tokens.Spacing.m))

                OutlinedTextField(
                    value = bio,
                    onValueChange = onBioChange,
                    label = { Text("Bio") },
                    placeholder = { Text("Tell neighbors about yourself...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.l))

        FilledTonalButton(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && displayName.isNotBlank(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                Text("Creating Profile...")
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Tokens.Spacing.s))
                Text("Create Profile")
            }
        }

        Spacer(modifier = Modifier.height(Tokens.Spacing.xl))
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isEmpty: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isEmpty) FontWeight.Normal else FontWeight.Medium,
            color = if (isEmpty)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VerificationBadge(status: VerificationStatus) {
    val (icon, text, color) = when (status) {
        VerificationStatus.VERIFIED -> Triple(
            Icons.Default.Verified,
            "Verified Member",
            Colors.Seed
        )
        VerificationStatus.EMAIL_PENDING -> Triple(
            Icons.Default.Email,
            "Email Pending",
            MaterialTheme.colorScheme.tertiary
        )
        VerificationStatus.UNVERIFIED -> Triple(
            Icons.Default.Info,
            "Unverified",
            MaterialTheme.colorScheme.outline
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Tokens.Spacing.s, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
