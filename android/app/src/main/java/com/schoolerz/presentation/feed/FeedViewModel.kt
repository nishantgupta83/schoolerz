package com.schoolerz.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val filter: PostType? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state = _state.asStateFlow()

    val filteredPosts: List<Post>
        get() = _state.value.filter?.let { f -> _state.value.posts.filter { it.type == f } }
            ?: _state.value.posts

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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun createPost(type: PostType, body: String, neighborhood: String) {
        val post = Post(
            type = type,
            authorId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous",
            authorName = "Current User",
            neighborhood = neighborhood,
            body = body
        )
        viewModelScope.launch {
            repository.createPost(post)
                .onSuccess { _state.update { it.copy(posts = listOf(post) + it.posts) } }
        }
    }
}
