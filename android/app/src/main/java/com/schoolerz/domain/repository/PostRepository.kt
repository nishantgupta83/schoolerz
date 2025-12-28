package com.schoolerz.domain.repository

import com.schoolerz.domain.model.Post

interface PostRepository {
    suspend fun fetchPosts(): Result<List<Post>>
    suspend fun createPost(post: Post): Result<Unit>
}
