package com.schoolerz.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class CommentTest {

    @Test
    fun `comment creation with default values`() {
        val comment = Comment(
            postId = "post123",
            authorId = "user123",
            authorName = "John Doe",
            text = "This is a comment"
        )

        assertNotNull(comment.id)
        assertEquals("post123", comment.postId)
        assertEquals("user123", comment.authorId)
        assertEquals("John Doe", comment.authorName)
        assertEquals("This is a comment", comment.text)
        assertNotNull(comment.createdAt)
    }

    @Test
    fun `comment creation with all values`() {
        val createdDate = Date()
        val comment = Comment(
            id = "comment-id",
            postId = "post456",
            authorId = "user456",
            authorName = "Jane Smith",
            text = "Great post!",
            createdAt = createdDate
        )

        assertEquals("comment-id", comment.id)
        assertEquals("post456", comment.postId)
        assertEquals("user456", comment.authorId)
        assertEquals("Jane Smith", comment.authorName)
        assertEquals("Great post!", comment.text)
        assertEquals(createdDate, comment.createdAt)
    }

    @Test
    fun `toFirestoreMap converts comment correctly`() {
        val comment = Comment(
            id = "test-id",
            postId = "post123",
            authorId = "user123",
            authorName = "John Doe",
            text = "Test comment"
        )

        val map = comment.toFirestoreMap()

        assertEquals("test-id", map["id"])
        assertEquals("post123", map["postId"])
        assertEquals("user123", map["authorId"])
        assertEquals("John Doe", map["authorName"])
        assertEquals("Test comment", map["text"])
        assertTrue(map.containsKey("createdAt"))
    }

    @Test
    fun `comment equality by values`() {
        val comment1 = Comment(
            id = "same-id",
            postId = "post1",
            authorId = "user123",
            authorName = "John",
            text = "Hello"
        )
        val comment2 = Comment(
            id = "same-id",
            postId = "post1",
            authorId = "user123",
            authorName = "John",
            text = "Hello"
        )

        assertEquals(comment1.id, comment2.id)
        assertEquals(comment1.postId, comment2.postId)
        assertEquals(comment1.text, comment2.text)
    }

    @Test
    fun `comment has unique id when not specified`() {
        val comment1 = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text 1"
        )
        val comment2 = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text 2"
        )

        assertNotEquals(comment1.id, comment2.id)
    }

    @Test
    fun `toFirestoreMap contains all required fields`() {
        val comment = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text"
        )

        val map = comment.toFirestoreMap()

        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("postId"))
        assertTrue(map.containsKey("authorId"))
        assertTrue(map.containsKey("authorName"))
        assertTrue(map.containsKey("text"))
        assertTrue(map.containsKey("createdAt"))
    }

    // MARK: - Edge Cases

    @Test
    fun `comment with empty text`() {
        val comment = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = ""
        )

        assertEquals("", comment.text)
    }

    @Test
    fun `comment with very long text`() {
        val longText = "A".repeat(1000)
        val comment = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = longText
        )

        assertEquals(1000, comment.text.length)
    }

    @Test
    fun `comment with special characters in text`() {
        val specialText = "Hello! @#$%^&*() \n\t 😀"
        val comment = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = specialText
        )

        assertEquals(specialText, comment.text)
    }

    @Test
    fun `comment copy creates new instance`() {
        val original = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "Original User",
            text = "Original text"
        )

        val copy = original.copy(text = "Modified text")

        assertEquals("Original text", original.text)
        assertEquals("Modified text", copy.text)
        assertEquals(original.id, copy.id)
        assertEquals(original.postId, copy.postId)
    }

    @Test
    fun `comment hashCode differs for different comments`() {
        val comment1 = Comment(
            id = "id1",
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text 1"
        )
        val comment2 = Comment(
            id = "id2",
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text 2"
        )

        assertNotEquals(comment1.hashCode(), comment2.hashCode())
    }

    @Test
    fun `comment with unicode author name`() {
        val comment = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "José García 中文",
            text = "Hello"
        )

        assertEquals("José García 中文", comment.authorName)
    }

    @Test
    fun `comment toString contains id`() {
        val comment = Comment(
            id = "specific-id",
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text"
        )

        assertTrue(comment.toString().contains("specific-id"))
    }

    @Test
    fun `toFirestoreMap createdAt is Date type`() {
        val comment = Comment(
            postId = "post1",
            authorId = "user1",
            authorName = "User",
            text = "Text"
        )

        val map = comment.toFirestoreMap()

        assertTrue(map["createdAt"] is Date)
    }

    @Test
    fun `multiple comments for same post have different ids`() {
        val comments = (1..5).map { i ->
            Comment(
                postId = "same-post",
                authorId = "user$i",
                authorName = "User $i",
                text = "Comment $i"
            )
        }

        val uniqueIds = comments.map { it.id }.toSet()
        assertEquals(5, uniqueIds.size)
    }
}
