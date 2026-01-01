package com.schoolerz.presentation.feed

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.presentation.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onOpenPost: (Post) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showComposer by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.fetchPosts() }

    // Show error in snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Retry",
                duration = SnackbarDuration.Long
            ).let { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.fetchPosts()
                }
                viewModel.clearError()
            }
        }
    }

    // Use derived StateFlow for filtered posts - single source of truth
    val filteredPosts by viewModel.filteredPosts.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showComposer = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Post")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            // Hero Section with search and categories
            item {
                HeroSection(
                    searchQuery = state.searchQuery,
                    onSearchChange = viewModel::onSearchChange,
                    selectedService = state.selectedService,
                    onServiceClick = viewModel::onServiceSelect
                )
            }

            // Post type filter (Offer/Request/All)
            item {
                FilterSegment(
                    selected = state.filter,
                    onSelect = { viewModel.setFilter(it) }
                )
            }

            // Content based on state
            when {
                state.isLoading -> {
                    items(5) {
                        ShimmerPostCard(modifier = Modifier.padding(Tokens.Spacing.m))
                    }
                }
                filteredPosts.isEmpty() && !state.isLoading -> {
                    item {
                        val hasFilters = state.searchQuery.isNotBlank() ||
                                        state.selectedService != null ||
                                        state.filter != null
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (hasFilters) "No posts match your search" else "No posts yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(Tokens.Spacing.s))
                                if (hasFilters) {
                                    TextButton(onClick = { viewModel.clearAllFilters() }) {
                                        Text("Clear filters")
                                    }
                                } else {
                                    Text(
                                        text = "Be the first to post!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    items(filteredPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onClick = { onOpenPost(post) },
                            modifier = Modifier.padding(horizontal = Tokens.Spacing.m, vertical = Tokens.Spacing.s)
                        )
                    }
                }
            }
        }
    }

    if (showComposer) {
        PostComposerSheet(
            onDismiss = { showComposer = false },
            onSubmit = { type, body, neighborhood ->
                viewModel.createPost(type, body, neighborhood)
                showComposer = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSegment(selected: PostType?, onSelect: (PostType?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Tokens.Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Tokens.Spacing.s)
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        FilterChip(selected = selected == PostType.OFFER, onClick = { onSelect(PostType.OFFER) }, label = { Text("Offers") })
        FilterChip(selected = selected == PostType.REQUEST, onClick = { onSelect(PostType.REQUEST) }, label = { Text("Requests") })
    }
}
