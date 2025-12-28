package com.schoolerz.data.mock

import com.schoolerz.domain.model.Comment
import com.schoolerz.domain.repository.CommentsRepository
import kotlinx.coroutines.delay

class MockCommentsRepository : CommentsRepository {
    private val comments = mutableMapOf<String, MutableList<Comment>>()

    override suspend fun fetchComments(postId: String): Result<List<Comment>> {
        delay(300)
        return Result.success(comments[postId]?.sortedBy { it.createdAt } ?: emptyList())
    }

    override suspend fun addComment(comment: Comment): Result<Unit> {
        delay(200)
        comments.getOrPut(comment.postId) { mutableListOf() }.add(comment)
        return Result.success(Unit)
    }
}
