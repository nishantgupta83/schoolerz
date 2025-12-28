package com.schoolerz.data.mock

import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.repository.PostRepository
import kotlinx.coroutines.delay

class MockPostRepository : PostRepository {
    private val posts = mutableListOf(
        Post(type = PostType.OFFER, authorId = "1", authorName = "Alex Kim", neighborhood = "Downtown", body = "Available for dog walking after school! $10/hour", likeCount = 12, commentCount = 3),
        Post(type = PostType.REQUEST, authorId = "2", authorName = "Sarah Johnson", neighborhood = "Westside", body = "Looking for a teen to help with yard work. $15/hour", likeCount = 5, commentCount = 2),
        Post(type = PostType.OFFER, authorId = "3", authorName = "Marcus Chen", neighborhood = "Eastside", body = "Math tutoring available! Can help with algebra through calculus.", likeCount = 24, commentCount = 7),
        Post(type = PostType.REQUEST, authorId = "4", authorName = "Emily Rodriguez", neighborhood = "Northgate", body = "Need someone to walk my kids home from school. Must be 16+", likeCount = 8, commentCount = 4),
        Post(type = PostType.OFFER, authorId = "5", authorName = "Jordan Lee", neighborhood = "Downtown", body = "Tech help for seniors! Phones, tablets, computers.", likeCount = 31, commentCount = 9)
    )

    override suspend fun fetchPosts(): Result<List<Post>> {
        delay(500)
        return Result.success(posts.sortedByDescending { it.createdAt })
    }

    override suspend fun createPost(post: Post): Result<Unit> {
        delay(300)
        posts.add(0, post)
        return Result.success(Unit)
    }
}
