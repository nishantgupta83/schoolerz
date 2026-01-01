package com.schoolerz.data.mock

import com.schoolerz.domain.model.Comment
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class MockCommentsRepositoryTest {

    private lateinit var repository: MockCommentsRepository

    @Before
    fun setup() {
        repository = MockCommentsRepository()
    }

    @Test
    fun `fetchComments returns empty list for new post`() = runTest {
        val result = repository.fetchComments("new-post-id")

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Comment>(), result.getOrNull())
    }

    @Test
    fun `addComment returns success`() = runTest {
        val comment = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "John Doe",
            text = "Great post!"
        )

        val result = repository.addComment(comment)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `addComment then fetchComments returns the comment`() = runTest {
        val comment = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "John Doe",
            text = "Great post!"
        )

        repository.addComment(comment)
        val result = repository.fetchComments("post-1")

        assertTrue(result.isSuccess)
        val comments = result.getOrNull() ?: emptyList()
        assertEquals(1, comments.size)
        assertEquals("Great post!", comments[0].text)
    }

    @Test
    fun `multiple comments on same post`() = runTest {
        val comment1 = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "User One",
            text = "First comment"
        )
        val comment2 = Comment(
            postId = "post-1",
            authorId = "user-2",
            authorName = "User Two",
            text = "Second comment"
        )

        repository.addComment(comment1)
        repository.addComment(comment2)

        val result = repository.fetchComments("post-1")
        val comments = result.getOrNull() ?: emptyList()

        assertEquals(2, comments.size)
    }

    @Test
    fun `comments on different posts are separate`() = runTest {
        val comment1 = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "User One",
            text = "Comment on post 1"
        )
        val comment2 = Comment(
            postId = "post-2",
            authorId = "user-2",
            authorName = "User Two",
            text = "Comment on post 2"
        )

        repository.addComment(comment1)
        repository.addComment(comment2)

        val result1 = repository.fetchComments("post-1")
        val result2 = repository.fetchComments("post-2")

        assertEquals(1, result1.getOrNull()?.size)
        assertEquals(1, result2.getOrNull()?.size)
        assertEquals("Comment on post 1", result1.getOrNull()?.first()?.text)
        assertEquals("Comment on post 2", result2.getOrNull()?.first()?.text)
    }

    @Test
    fun `comments are sorted by createdAt`() = runTest {
        val oldDate = Date(System.currentTimeMillis() - 10000)
        val newDate = Date(System.currentTimeMillis())

        val oldComment = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "User",
            text = "Old comment",
            createdAt = oldDate
        )
        val newComment = Comment(
            postId = "post-1",
            authorId = "user-2",
            authorName = "User",
            text = "New comment",
            createdAt = newDate
        )

        // Add in reverse order
        repository.addComment(newComment)
        repository.addComment(oldComment)

        val result = repository.fetchComments("post-1")
        val comments = result.getOrNull() ?: emptyList()

        assertEquals(2, comments.size)
        assertEquals("Old comment", comments[0].text)
        assertEquals("New comment", comments[1].text)
    }

    @Test
    fun `fetchComments for non-existent post returns empty list`() = runTest {
        val result = repository.fetchComments("non-existent-post")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `comment preserves all fields`() = runTest {
        val comment = Comment(
            id = "custom-id",
            postId = "post-1",
            authorId = "user-1",
            authorName = "John Doe",
            text = "Test comment"
        )

        repository.addComment(comment)
        val result = repository.fetchComments("post-1")
        val fetchedComment = result.getOrNull()?.first()

        assertNotNull(fetchedComment)
        assertEquals("custom-id", fetchedComment?.id)
        assertEquals("post-1", fetchedComment?.postId)
        assertEquals("user-1", fetchedComment?.authorId)
        assertEquals("John Doe", fetchedComment?.authorName)
        assertEquals("Test comment", fetchedComment?.text)
    }

    @Test
    fun `multiple repositories are independent`() = runTest {
        val repo1 = MockCommentsRepository()
        val repo2 = MockCommentsRepository()

        val comment = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "User",
            text = "Comment"
        )

        repo1.addComment(comment)

        val result1 = repo1.fetchComments("post-1")
        val result2 = repo2.fetchComments("post-1")

        assertEquals(1, result1.getOrNull()?.size)
        assertEquals(0, result2.getOrNull()?.size)
    }

    @Test
    fun `many comments on same post`() = runTest {
        val postId = "busy-post"

        repeat(10) { i ->
            val comment = Comment(
                postId = postId,
                authorId = "user-$i",
                authorName = "User $i",
                text = "Comment $i"
            )
            repository.addComment(comment)
        }

        val result = repository.fetchComments(postId)
        val comments = result.getOrNull() ?: emptyList()

        assertEquals(10, comments.size)
    }

    @Test
    fun `comment with special characters`() = runTest {
        val comment = Comment(
            postId = "post-1",
            authorId = "user-1",
            authorName = "José García",
            text = "Great post! 🎉 #awesome @mention"
        )

        repository.addComment(comment)
        val result = repository.fetchComments("post-1")
        val fetchedComment = result.getOrNull()?.first()

        assertEquals("José García", fetchedComment?.authorName)
        assertEquals("Great post! 🎉 #awesome @mention", fetchedComment?.text)
    }
}
