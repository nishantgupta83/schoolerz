package com.schoolerz.domain.repository

import com.schoolerz.domain.model.Comment

interface CommentsRepository {
    suspend fun fetchComments(postId: String): Result<List<Comment>>
    suspend fun addComment(comment: Comment): Result<Unit>
}
