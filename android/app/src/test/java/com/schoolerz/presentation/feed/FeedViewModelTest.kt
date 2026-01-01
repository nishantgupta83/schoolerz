package com.schoolerz.presentation.feed

import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.model.RateType
import com.schoolerz.domain.model.ServiceType
import com.schoolerz.domain.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testPosts = listOf(
        Post(
            id = "post1",
            type = PostType.OFFER,
            authorId = "user1",
            authorName = "John Doe",
            neighborhood = "Downtown",
            body = "Offering tutoring services"
        ),
        Post(
            id = "post2",
            type = PostType.REQUEST,
            authorId = "user2",
            authorName = "Jane Smith",
            neighborhood = "Uptown",
            body = "Need help with lawn care"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `PostType enum has correct values`() {
        assertEquals(2, PostType.values().size)
        assertTrue(PostType.values().contains(PostType.OFFER))
        assertTrue(PostType.values().contains(PostType.REQUEST))
    }

    @Test
    fun `test posts have correct types`() {
        val offerPost = testPosts[0]
        val requestPost = testPosts[1]

        assertEquals(PostType.OFFER, offerPost.type)
        assertEquals(PostType.REQUEST, requestPost.type)
    }

    @Test
    fun `test post data is correct`() {
        val post = testPosts[0]

        assertEquals("post1", post.id)
        assertEquals("user1", post.authorId)
        assertEquals("John Doe", post.authorName)
        assertEquals("Downtown", post.neighborhood)
        assertEquals("Offering tutoring services", post.body)
    }

    @Test
    fun `filtering posts by type works`() {
        val offers = testPosts.filter { it.type == PostType.OFFER }
        val requests = testPosts.filter { it.type == PostType.REQUEST }

        assertEquals(1, offers.size)
        assertEquals(1, requests.size)
        assertEquals("post1", offers[0].id)
        assertEquals("post2", requests[0].id)
    }

    @Test
    fun `FeedState default values are correct`() {
        val state = FeedState()

        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.posts.isEmpty())
        assertNull(state.error)
        assertNull(state.filter)
    }

    @Test
    fun `FeedState with posts`() {
        val state = FeedState(
            isLoading = false,
            posts = testPosts,
            error = null,
            filter = PostType.OFFER
        )

        assertFalse(state.isLoading)
        assertEquals(2, state.posts.size)
        assertEquals(PostType.OFFER, state.filter)
    }

    @Test
    fun `FeedState with error`() {
        val state = FeedState(
            isLoading = false,
            posts = emptyList(),
            error = "Network error"
        )

        assertFalse(state.isLoading)
        assertTrue(state.posts.isEmpty())
        assertEquals("Network error", state.error)
    }

    @Test
    fun `FeedState loading state`() {
        val state = FeedState(isLoading = true)

        assertTrue(state.isLoading)
        assertTrue(state.posts.isEmpty())
    }

    @Test
    fun `FeedState refreshing state`() {
        val state = FeedState(
            isRefreshing = true,
            posts = testPosts
        )

        assertTrue(state.isRefreshing)
        assertEquals(2, state.posts.size)
    }

    // MARK: - Search Query Tests

    @Test
    fun `FeedState searchQuery default is empty`() {
        val state = FeedState()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `FeedState searchQuery can be set`() {
        val state = FeedState(searchQuery = "dog walking")
        assertEquals("dog walking", state.searchQuery)
    }

    // MARK: - Service Filter Tests

    @Test
    fun `FeedState selectedService default is null`() {
        val state = FeedState()
        assertNull(state.selectedService)
    }

    @Test
    fun `FeedState selectedService can be set`() {
        val state = FeedState(selectedService = ServiceType.DOG_WALKING)
        assertEquals(ServiceType.DOG_WALKING, state.selectedService)
    }

    // MARK: - Combined Filters Tests

    @Test
    fun `FeedState with all filters set`() {
        val state = FeedState(
            posts = testPosts,
            filter = PostType.OFFER,
            searchQuery = "tutoring",
            selectedService = ServiceType.TUTORING
        )

        assertEquals(2, state.posts.size)
        assertEquals(PostType.OFFER, state.filter)
        assertEquals("tutoring", state.searchQuery)
        assertEquals(ServiceType.TUTORING, state.selectedService)
    }

    @Test
    fun `filtering by service type works`() {
        val postsWithService = listOf(
            Post(
                id = "post1",
                type = PostType.OFFER,
                authorId = "user1",
                authorName = "John",
                neighborhood = "Downtown",
                body = "Dog walking",
                serviceType = ServiceType.DOG_WALKING
            ),
            Post(
                id = "post2",
                type = PostType.OFFER,
                authorId = "user2",
                authorName = "Jane",
                neighborhood = "Uptown",
                body = "Tutoring",
                serviceType = ServiceType.TUTORING
            )
        )

        val dogWalkingPosts = postsWithService.filter { it.serviceType == ServiceType.DOG_WALKING }
        assertEquals(1, dogWalkingPosts.size)
        assertEquals("post1", dogWalkingPosts[0].id)
    }

    @Test
    fun `filtering by multiple criteria works`() {
        val postsWithService = listOf(
            Post(
                id = "post1",
                type = PostType.OFFER,
                authorId = "user1",
                authorName = "John",
                neighborhood = "Downtown",
                body = "Dog walking",
                serviceType = ServiceType.DOG_WALKING
            ),
            Post(
                id = "post2",
                type = PostType.REQUEST,
                authorId = "user2",
                authorName = "Jane",
                neighborhood = "Uptown",
                body = "Need dog walker",
                serviceType = ServiceType.DOG_WALKING
            ),
            Post(
                id = "post3",
                type = PostType.OFFER,
                authorId = "user3",
                authorName = "Bob",
                neighborhood = "Midtown",
                body = "Tutoring",
                serviceType = ServiceType.TUTORING
            )
        )

        val filtered = postsWithService
            .filter { it.type == PostType.OFFER }
            .filter { it.serviceType == ServiceType.DOG_WALKING }

        assertEquals(1, filtered.size)
        assertEquals("post1", filtered[0].id)
    }

    // MARK: - Post Computed Properties Tests

    @Test
    fun `Post authorInitials returns correct initials`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John Doe",
            neighborhood = "Downtown",
            body = "Service"
        )

        assertEquals("JD", post.authorInitials)
    }

    @Test
    fun `Post authorInitials handles single name`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Service"
        )

        assertEquals("J", post.authorInitials)
    }

    @Test
    fun `Post formattedPrice returns correct format`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Service",
            rateAmount = 15.0,
            rateType = RateType.HOURLY
        )

        assertEquals("$15/hour", post.formattedPrice)
    }

    @Test
    fun `Post formattedPrice with range`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Service",
            rateAmount = 15.0,
            rateMax = 25.0,
            rateType = RateType.HOURLY
        )

        assertEquals("$15-25/hour", post.formattedPrice)
    }

    @Test
    fun `Post formattedAvailability returns correct format`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Service",
            availableDays = listOf("Monday", "Wednesday", "Friday"),
            availableTimeStart = "3:00 PM",
            availableTimeEnd = "6:00 PM"
        )

        val availability = post.formattedAvailability
        assertNotNull(availability)
        // Days are truncated to first 3 chars: "Mon, Wed, Fri"
        assertTrue(availability!!.contains("Mon"))
        assertTrue(availability.contains("3:00 PM"))
    }

    @Test
    fun `Post formattedAvailability returns null when no days`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Service"
        )

        assertNull(post.formattedAvailability)
    }
}
