package com.schoolerz.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolerz.core.util.SemanticSearch
import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.model.ServiceType
import com.schoolerz.domain.repository.AuthService
import com.schoolerz.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val filter: PostType? = null,
    val searchQuery: String = "",
    val selectedService: ServiceType? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: PostRepository,
    private val authService: AuthService
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state = _state.asStateFlow()

    // Derived StateFlow for filtered posts using semantic search
    val filteredPosts = _state.map { state ->
        // Use SemanticSearch for intelligent filtering and ranking
        SemanticSearch.searchPosts(
            posts = state.posts,
            query = state.searchQuery.trim(),
            serviceType = state.selectedService,
            postType = state.filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.fetchPosts()
                .onSuccess { posts -> _state.update { it.copy(posts = posts, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            repository.fetchPosts()
                .onSuccess { posts -> _state.update { it.copy(posts = posts, isRefreshing = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isRefreshing = false) } }
        }
    }

    fun setFilter(filter: PostType?) {
        _state.update { it.copy(filter = filter) }
    }

    fun onSearchChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onServiceSelect(service: ServiceType?) {
        _state.update { currentState ->
            if (service == null) {
                // "All" clicked - clear service filter
                currentState.copy(selectedService = null)
            } else {
                // Toggle selection - if same service clicked, deselect
                val newSelection = if (currentState.selectedService == service) null else service
                currentState.copy(selectedService = newSelection)
            }
        }
    }

    fun clearSearch() {
        _state.update { it.copy(searchQuery = "") }
    }

    fun clearAllFilters() {
        _state.update { it.copy(searchQuery = "", selectedService = null, filter = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // Check if any filters are active
    val hasActiveFilters: Boolean
        get() = _state.value.searchQuery.isNotBlank() ||
                _state.value.selectedService != null ||
                _state.value.filter != null

    fun createPost(type: PostType, body: String, neighborhood: String) {
        val post = Post(
            type = type,
            authorId = authService.currentUserId(),
            authorName = authService.currentUserName(),
            neighborhood = neighborhood,
            body = body
        )
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            repository.createPost(post)
                .onSuccess { _state.update { it.copy(posts = listOf(post) + it.posts) } }
                .onFailure { e -> _state.update { it.copy(error = e.message ?: "Failed to create post") } }
        }
    }
}
