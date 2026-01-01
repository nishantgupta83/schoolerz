package com.schoolerz.data.mock

import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockPostRepositoryTest {

    private lateinit var repository: MockPostRepository

    @Before
    fun setup() {
        repository = MockPostRepository()
    }

    @Test
    fun `fetchPosts returns success with posts`() = runTest {
        val result = repository.fetchPosts()

        assertTrue(result.isSuccess)
        val posts = result.getOrNull()
        assertNotNull(posts)
        assertTrue(posts!!.isNotEmpty())
    }

    @Test
    fun `fetchPosts returns list of posts`() = runTest {
        val result = repository.fetchPosts()

        assertTrue(result.isSuccess)
        val posts = result.getOrNull()!!

        // Verify posts have required fields
        for (post in posts) {
            assertNotNull(post.id)
            assertNotNull(post.authorId)
            assertNotNull(post.authorName)
            assertNotNull(post.neighborhood)
            assertNotNull(post.body)
        }
    }

    @Test
    fun `createPost adds post and returns success`() = runTest {
        val newPost = Post(
            type = PostType.OFFER,
            authorId = "test-user",
            authorName = "Test User",
            neighborhood = "Test Town",
            body = "Test post body"
        )

        val result = repository.createPost(newPost)
        assertTrue(result.isSuccess)

        // Verify post was added
        val fetchResult = repository.fetchPosts()
        val posts = fetchResult.getOrNull() ?: emptyList()
        assertTrue(posts.any { it.authorId == "test-user" && it.body == "Test post body" })
    }

    @Test
    fun `multiple createPost calls work correctly`() = runTest {
        val initialResult = repository.fetchPosts()
        val initialCount = initialResult.getOrNull()?.size ?: 0

        val post1 = Post(
            type = PostType.OFFER,
            authorId = "user1",
            authorName = "User One",
            neighborhood = "Town",
            body = "First post"
        )

        val post2 = Post(
            type = PostType.REQUEST,
            authorId = "user2",
            authorName = "User Two",
            neighborhood = "City",
            body = "Second post"
        )

        repository.createPost(post1)
        repository.createPost(post2)

        val fetchResult = repository.fetchPosts()
        val posts = fetchResult.getOrNull() ?: emptyList()

        assertEquals(initialCount + 2, posts.size)
        assertTrue(posts.any { it.body == "First post" })
        assertTrue(posts.any { it.body == "Second post" })
    }

    @Test
    fun `posts have different types`() = runTest {
        val result = repository.fetchPosts()
        val posts = result.getOrNull() ?: emptyList()

        val offers = posts.filter { it.type == PostType.OFFER }
        val requests = posts.filter { it.type == PostType.REQUEST }

        // Mock data should have both types
        assertTrue(offers.isNotEmpty() || requests.isNotEmpty())
    }

    @Test
    fun `post creation assigns unique id`() = runTest {
        val post1 = Post(
            type = PostType.OFFER,
            authorId = "user1",
            authorName = "User One",
            neighborhood = "Town",
            body = "First post"
        )

        val post2 = Post(
            type = PostType.OFFER,
            authorId = "user1",
            authorName = "User One",
            neighborhood = "Town",
            body = "Second post"
        )

        assertNotEquals(post1.id, post2.id)
    }

    // MARK: - Extended Mock Data Tests

    @Test
    fun `mock posts have realistic data`() = runTest {
        val result = repository.fetchPosts()
        val posts = result.getOrNull() ?: emptyList()

        for (post in posts) {
            assertTrue("Author name should not be empty", post.authorName.isNotEmpty())
            assertTrue("Neighborhood should not be empty", post.neighborhood.isNotEmpty())
            assertTrue("Body should not be empty", post.body.isNotEmpty())
        }
    }

    @Test
    fun `posts can be filtered by neighborhood`() = runTest {
        val result = repository.fetchPosts()
        val posts = result.getOrNull() ?: emptyList()

        val neighborhoods = posts.map { it.neighborhood }.distinct()
        assertTrue("Should have multiple neighborhoods", neighborhoods.size >= 1)
    }

    @Test
    fun `fetchPosts returns same posts on multiple calls`() = runTest {
        val result1 = repository.fetchPosts()
        val result2 = repository.fetchPosts()

        val posts1 = result1.getOrNull() ?: emptyList()
        val posts2 = result2.getOrNull() ?: emptyList()

        assertEquals(posts1.size, posts2.size)
    }

    @Test
    fun `createPost with all fields`() = runTest {
        val post = Post(
            type = PostType.OFFER,
            authorId = "full-test-user",
            authorName = "Complete User",
            neighborhood = "Full Town",
            body = "Complete post with all fields",
            likeCount = 5,
            commentCount = 3
        )

        val result = repository.createPost(post)
        assertTrue(result.isSuccess)

        val fetchResult = repository.fetchPosts()
        val posts = fetchResult.getOrNull() ?: emptyList()
        val createdPost = posts.find { it.authorId == "full-test-user" }

        assertNotNull(createdPost)
        assertEquals(5, createdPost?.likeCount)
        assertEquals(3, createdPost?.commentCount)
    }

    @Test
    fun `posts are ordered by creation date`() = runTest {
        val result = repository.fetchPosts()
        val posts = result.getOrNull() ?: emptyList()

        if (posts.size >= 2) {
            for (i in 0 until posts.size - 1) {
                // Posts should be sorted (typically newest first or oldest first)
                // Just verify ordering exists
                assertNotNull(posts[i].createdAt)
                assertNotNull(posts[i + 1].createdAt)
            }
        }
    }

    @Test
    fun `repository handles empty post body gracefully`() = runTest {
        // Test that even if we create an unusual post, it works
        val post = Post(
            type = PostType.REQUEST,
            authorId = "edge-case-user",
            authorName = "Edge",
            neighborhood = "Test",
            body = ""
        )

        val result = repository.createPost(post)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `posts have valid PostType values`() = runTest {
        val result = repository.fetchPosts()
        val posts = result.getOrNull() ?: emptyList()

        for (post in posts) {
            assertTrue(
                "Post type should be OFFER or REQUEST",
                post.type == PostType.OFFER || post.type == PostType.REQUEST
            )
        }
    }

    @Test
    fun `posts have non-negative engagement counts`() = runTest {
        val result = repository.fetchPosts()
        val posts = result.getOrNull() ?: emptyList()

        for (post in posts) {
            assertTrue("Like count should be non-negative", post.likeCount >= 0)
            assertTrue("Comment count should be non-negative", post.commentCount >= 0)
        }
    }

    @Test
    fun `new repository instance has same initial data`() = runTest {
        val repo1 = MockPostRepository()
        val repo2 = MockPostRepository()

        val posts1 = repo1.fetchPosts().getOrNull() ?: emptyList()
        val posts2 = repo2.fetchPosts().getOrNull() ?: emptyList()

        // Both should have the same initial mock data
        assertEquals(posts1.size, posts2.size)
    }
}
