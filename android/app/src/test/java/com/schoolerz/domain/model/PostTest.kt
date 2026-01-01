package com.schoolerz.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class PostTest {

    @Test
    fun `post creation with default values`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John Doe",
            neighborhood = "Downtown",
            body = "Test post body"
        )

        assertNotNull(post.id)
        assertEquals(PostType.OFFER, post.type)
        assertEquals("user123", post.authorId)
        assertEquals("John Doe", post.authorName)
        assertEquals("Downtown", post.neighborhood)
        assertEquals("Test post body", post.body)
        assertEquals(0, post.likeCount)
        assertEquals(0, post.commentCount)
        assertNotNull(post.createdAt)
    }

    @Test
    fun `post creation with all values`() {
        val createdDate = Date()
        val post = Post(
            id = "custom-id",
            type = PostType.REQUEST,
            authorId = "user456",
            authorName = "Jane Smith",
            neighborhood = "Uptown",
            body = "Looking for help",
            likeCount = 10,
            commentCount = 5,
            createdAt = createdDate
        )

        assertEquals("custom-id", post.id)
        assertEquals(PostType.REQUEST, post.type)
        assertEquals(10, post.likeCount)
        assertEquals(5, post.commentCount)
        assertEquals(createdDate, post.createdAt)
    }

    @Test
    fun `author initials from full name`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John Doe",
            neighborhood = "Downtown",
            body = "Test"
        )

        assertEquals("JD", post.authorInitials)
    }

    @Test
    fun `author initials from single name`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test"
        )

        assertEquals("J", post.authorInitials)
    }

    @Test
    fun `author initials from three word name`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John Michael Doe",
            neighborhood = "Downtown",
            body = "Test"
        )

        assertEquals("JM", post.authorInitials) // Only takes first 2
    }

    @Test
    fun `toFirestoreMap converts post correctly`() {
        val post = Post(
            id = "test-id",
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John Doe",
            neighborhood = "Downtown",
            body = "Test body",
            likeCount = 5,
            commentCount = 3
        )

        val map = post.toFirestoreMap()

        assertEquals("test-id", map["id"])
        assertEquals("offer", map["type"])
        assertEquals("user123", map["authorId"])
        assertEquals("John Doe", map["authorName"])
        assertEquals("Downtown", map["neighborhood"])
        assertEquals("Test body", map["body"])
        assertEquals(5, map["likeCount"])
        assertEquals(3, map["commentCount"])
    }

    @Test
    fun `PostType enum values`() {
        assertEquals(2, PostType.values().size)
        assertTrue(PostType.values().contains(PostType.OFFER))
        assertTrue(PostType.values().contains(PostType.REQUEST))
    }

    @Test
    fun `PostType valueOf works correctly`() {
        assertEquals(PostType.OFFER, PostType.valueOf("OFFER"))
        assertEquals(PostType.REQUEST, PostType.valueOf("REQUEST"))
    }

    // MARK: - Pricing Tests

    @Test
    fun `formattedPrice with hourly rate`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            rateAmount = 15.0,
            rateType = RateType.HOURLY
        )

        assertEquals("$15/hour", post.formattedPrice)
    }

    @Test
    fun `formattedPrice with per task rate`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            rateAmount = 30.0,
            rateType = RateType.PER_TASK
        )

        assertEquals("$30/task", post.formattedPrice)
    }

    @Test
    fun `formattedPrice with negotiable rate`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            rateType = RateType.NEGOTIABLE
        )

        assertEquals("Negotiable", post.formattedPrice)
    }

    @Test
    fun `formattedPrice with rate range`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            rateAmount = 15.0,
            rateMax = 25.0,
            rateType = RateType.HOURLY
        )

        assertEquals("$15-25/hour", post.formattedPrice)
    }

    // MARK: - Availability Tests

    @Test
    fun `formattedAvailability with days and time`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            availableDays = listOf("Monday", "Wednesday", "Friday"),
            availableTimeStart = "3:00 PM",
            availableTimeEnd = "6:00 PM"
        )

        val availability = post.formattedAvailability
        assertNotNull(availability)
        assertTrue(availability!!.contains("Mon"))
        assertTrue(availability.contains("Wed"))
        assertTrue(availability.contains("Fri"))
        assertTrue(availability.contains("3:00 PM"))
        assertTrue(availability.contains("6:00 PM"))
    }

    @Test
    fun `formattedAvailability with days only`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            availableDays = listOf("Saturday", "Sunday")
        )

        val availability = post.formattedAvailability
        assertNotNull(availability)
        assertTrue(availability!!.contains("Sat"))
        assertTrue(availability.contains("Sun"))
        assertFalse(availability.contains(" - "))
    }

    @Test
    fun `formattedAvailability null when no days`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test"
        )

        assertNull(post.formattedAvailability)
    }

    // MARK: - Service Type and Experience Level Tests

    @Test
    fun `post with service type`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Dog walking services",
            serviceType = ServiceType.DOG_WALKING
        )

        assertEquals(ServiceType.DOG_WALKING, post.serviceType)
    }

    @Test
    fun `post with experience level`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            experienceLevel = ExperienceLevel.EXPERIENCED
        )

        assertEquals(ExperienceLevel.EXPERIENCED, post.experienceLevel)
    }

    @Test
    fun `post with skill tags`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            skillTags = listOf("CPR Certified", "Patient", "Bilingual")
        )

        assertEquals(3, post.skillTags.size)
        assertTrue(post.skillTags.contains("CPR Certified"))
        assertTrue(post.skillTags.contains("Patient"))
        assertTrue(post.skillTags.contains("Bilingual"))
    }

    // MARK: - toFirestoreMap Extended Tests

    @Test
    fun `toFirestoreMap includes all pricing fields`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            rateAmount = 20.0,
            rateMax = 30.0,
            rateType = RateType.HOURLY
        )

        val map = post.toFirestoreMap()

        assertEquals(20.0, map["rateAmount"])
        assertEquals(30.0, map["rateMax"])
        assertEquals("hourly", map["rateType"])
    }

    @Test
    fun `toFirestoreMap includes availability fields`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            availableDays = listOf("Monday", "Tuesday"),
            availableTimeStart = "9:00 AM",
            availableTimeEnd = "5:00 PM"
        )

        val map = post.toFirestoreMap()

        assertEquals(listOf("Monday", "Tuesday"), map["availableDays"])
        assertEquals("9:00 AM", map["availableTimeStart"])
        assertEquals("5:00 PM", map["availableTimeEnd"])
    }

    @Test
    fun `toFirestoreMap includes service fields`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test",
            serviceType = ServiceType.TUTORING,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            skillTags = listOf("Math", "Science")
        )

        val map = post.toFirestoreMap()

        assertEquals("tutoring", map["serviceType"])
        assertEquals("intermediate", map["experienceLevel"])
        assertEquals(listOf("Math", "Science"), map["skillTags"])
    }

    // MARK: - Edge Cases

    @Test
    fun `author initials with empty name`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "",
            neighborhood = "Downtown",
            body = "Test"
        )

        assertEquals("", post.authorInitials)
    }

    @Test
    fun `author initials with lowercase name`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "john doe",
            neighborhood = "Downtown",
            body = "Test"
        )

        assertEquals("JD", post.authorInitials)
    }

    @Test
    fun `post equality based on id`() {
        val post1 = Post(
            id = "same-id",
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Test 1"
        )

        val post2 = Post(
            id = "same-id",
            type = PostType.REQUEST,
            authorId = "user456",
            authorName = "Jane",
            neighborhood = "Uptown",
            body = "Test 2"
        )

        // Data class equality is based on all fields
        assertNotEquals(post1, post2)
    }

    @Test
    fun `post copy creates new instance`() {
        val original = Post(
            type = PostType.OFFER,
            authorId = "user123",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Original"
        )

        val copy = original.copy(body = "Modified")

        assertEquals("Original", original.body)
        assertEquals("Modified", copy.body)
        assertEquals(original.id, copy.id)
    }
}
