package com.schoolerz.core.util

import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.model.ServiceType
import org.junit.Assert.*
import org.junit.Test

class SemanticSearchTest {

    // MARK: - Query Expansion Tests

    @Test
    fun `expandQuery with dog walking returns synonyms`() {
        val expanded = SemanticSearch.expandQuery("dog walking")

        assertTrue(expanded.contains("dog walking"))
        assertTrue(expanded.contains("dog walker"))
        assertTrue(expanded.contains("pet care"))
        assertTrue(expanded.contains("walk my dog"))
    }

    @Test
    fun `expandQuery with tutoring returns synonyms`() {
        val expanded = SemanticSearch.expandQuery("tutoring")

        assertTrue(expanded.contains("tutoring"))
        assertTrue(expanded.contains("tutor"))
        assertTrue(expanded.contains("homework help"))
        assertTrue(expanded.contains("math help"))
    }

    @Test
    fun `expandQuery with babysitting returns synonyms`() {
        val expanded = SemanticSearch.expandQuery("babysitting")

        assertTrue(expanded.contains("babysitting"))
        assertTrue(expanded.contains("babysitter"))
        assertTrue(expanded.contains("childcare"))
        assertTrue(expanded.contains("nanny"))
    }

    @Test
    fun `expandQuery with no synonyms returns original`() {
        val expanded = SemanticSearch.expandQuery("random unique query")

        assertEquals(1, expanded.size)
        assertTrue(expanded.contains("random unique query"))
    }

    // MARK: - Intent Detection Tests

    @Test
    fun `detectIntent returns OFFER for offer keywords`() {
        assertEquals(PostType.OFFER, SemanticSearch.detectIntent("I am available for tutoring"))
        assertEquals(PostType.OFFER, SemanticSearch.detectIntent("offering dog walking"))
        assertEquals(PostType.OFFER, SemanticSearch.detectIntent("I can help with lawn care"))
        assertEquals(PostType.OFFER, SemanticSearch.detectIntent("providing babysitting services"))
    }

    @Test
    fun `detectIntent returns REQUEST for request keywords`() {
        assertEquals(PostType.REQUEST, SemanticSearch.detectIntent("I need a tutor"))
        assertEquals(PostType.REQUEST, SemanticSearch.detectIntent("looking for dog walker"))
        assertEquals(PostType.REQUEST, SemanticSearch.detectIntent("searching for babysitter"))
        assertEquals(PostType.REQUEST, SemanticSearch.detectIntent("help wanted for lawn care"))
    }

    @Test
    fun `detectIntent returns null for neutral queries`() {
        assertNull(SemanticSearch.detectIntent("dog walking"))
        assertNull(SemanticSearch.detectIntent("tutoring services"))
        assertNull(SemanticSearch.detectIntent("babysitting"))
    }

    // MARK: - Service Type Detection Tests

    @Test
    fun `detectServiceType returns DOG_WALKING for pet related queries`() {
        assertEquals(ServiceType.DOG_WALKING, SemanticSearch.detectServiceType("walk my dog"))
        assertEquals(ServiceType.DOG_WALKING, SemanticSearch.detectServiceType("need pet care"))
        assertEquals(ServiceType.DOG_WALKING, SemanticSearch.detectServiceType("looking for puppy sitter"))
    }

    @Test
    fun `detectServiceType returns TUTORING for education queries`() {
        assertEquals(ServiceType.TUTORING, SemanticSearch.detectServiceType("math tutor needed"))
        assertEquals(ServiceType.TUTORING, SemanticSearch.detectServiceType("homework help"))
        assertEquals(ServiceType.TUTORING, SemanticSearch.detectServiceType("test prep"))
    }

    @Test
    fun `detectServiceType returns BABYSITTING for childcare queries`() {
        // serviceMapping has: babysit, child, kids, nanny
        assertEquals(ServiceType.BABYSITTING, SemanticSearch.detectServiceType("need babysit help"))
        assertEquals(ServiceType.BABYSITTING, SemanticSearch.detectServiceType("kids care needed"))
        assertEquals(ServiceType.BABYSITTING, SemanticSearch.detectServiceType("looking for nanny"))
    }

    @Test
    fun `detectServiceType returns LAWN_CARE for yard queries`() {
        assertEquals(ServiceType.LAWN_CARE, SemanticSearch.detectServiceType("lawn mowing"))
        assertEquals(ServiceType.LAWN_CARE, SemanticSearch.detectServiceType("yard work"))
        assertEquals(ServiceType.LAWN_CARE, SemanticSearch.detectServiceType("grass cutting"))
    }

    @Test
    fun `detectServiceType returns TECH_HELP for technology queries`() {
        assertEquals(ServiceType.TECH_HELP, SemanticSearch.detectServiceType("computer help"))
        assertEquals(ServiceType.TECH_HELP, SemanticSearch.detectServiceType("phone repair"))
        assertEquals(ServiceType.TECH_HELP, SemanticSearch.detectServiceType("laptop assistance"))
    }

    @Test
    fun `detectServiceType returns null for unknown queries`() {
        assertNull(SemanticSearch.detectServiceType("random service"))
        assertNull(SemanticSearch.detectServiceType("something else"))
    }

    // MARK: - Match Score Tests

    @Test
    fun `matchScore returns high score for exact match`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Offering dog walking services"
        )

        val score = SemanticSearch.matchScore("dog walking", post)
        assertTrue(score > 1.0) // Exact match + synonyms
    }

    @Test
    fun `matchScore returns positive score for synonym match`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "I can provide pet care services"
        )

        // "pet care" is a synonym of "dog walking"
        val score = SemanticSearch.matchScore("dog walking", post)
        assertTrue("Score should be positive for synonym match: $score", score > 0.0)
    }

    @Test
    fun `matchScore returns zero for no match`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Offering lawn mowing services"
        )

        val score = SemanticSearch.matchScore("babysitting", post)
        assertEquals(0.0, score, 0.001)
    }

    @Test
    fun `matchScore returns positive for author name match`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John Smith",
            neighborhood = "Downtown",
            body = "Offering services"
        )

        val score = SemanticSearch.matchScore("john", post)
        assertTrue(score > 0.0)
    }

    @Test
    fun `matchScore returns positive for neighborhood match`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Offering services"
        )

        val score = SemanticSearch.matchScore("downtown", post)
        assertTrue(score > 0.0)
    }

    @Test
    fun `matchScore returns 1 for empty query`() {
        val post = Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "John",
            neighborhood = "Downtown",
            body = "Offering dog walking"
        )

        val score = SemanticSearch.matchScore("", post)
        assertEquals(1.0, score, 0.001)
    }

    // MARK: - Ranking Tests

    @Test
    fun `rankPosts orders by relevance score`() {
        val posts = listOf(
            Post(type = PostType.OFFER, authorId = "1", authorName = "A", neighborhood = "X", body = "Lawn care services"),
            Post(type = PostType.OFFER, authorId = "2", authorName = "B", neighborhood = "Y", body = "Dog walking available"),
            Post(type = PostType.OFFER, authorId = "3", authorName = "C", neighborhood = "Z", body = "Dog walker with experience")
        )

        val ranked = SemanticSearch.rankPosts(posts, "dog walking")

        assertEquals(2, ranked.size)
        assertEquals("B", ranked[0].authorName) // Higher score for exact match
    }

    @Test
    fun `rankPosts returns all for empty query`() {
        val posts = listOf(
            Post(type = PostType.OFFER, authorId = "1", authorName = "A", neighborhood = "X", body = "Service A"),
            Post(type = PostType.OFFER, authorId = "2", authorName = "B", neighborhood = "Y", body = "Service B")
        )

        val ranked = SemanticSearch.rankPosts(posts, "")

        assertEquals(2, ranked.size)
    }

    // MARK: - Search Posts Integration Tests

    @Test
    fun `searchPosts with query filters correctly`() {
        val posts = listOf(
            Post(type = PostType.OFFER, authorId = "1", authorName = "A", neighborhood = "X", body = "Dog walking"),
            Post(type = PostType.REQUEST, authorId = "2", authorName = "B", neighborhood = "Y", body = "Need tutor"),
            Post(type = PostType.OFFER, authorId = "3", authorName = "C", neighborhood = "Z", body = "Babysitting")
        )

        val results = SemanticSearch.searchPosts(posts, "dog", null, null)

        assertEquals(1, results.size)
        assertEquals("Dog walking", results[0].body)
    }

    @Test
    fun `searchPosts with service filter returns matching`() {
        val posts = listOf(
            Post(type = PostType.OFFER, authorId = "1", authorName = "A", neighborhood = "X", body = "Dog walking", serviceType = ServiceType.DOG_WALKING),
            Post(type = PostType.OFFER, authorId = "2", authorName = "B", neighborhood = "Y", body = "Tutoring", serviceType = ServiceType.TUTORING),
            Post(type = PostType.OFFER, authorId = "3", authorName = "C", neighborhood = "Z", body = "Pet care", serviceType = ServiceType.DOG_WALKING)
        )

        val results = SemanticSearch.searchPosts(posts, "", ServiceType.DOG_WALKING, null)

        assertEquals(2, results.size)
    }

    @Test
    fun `searchPosts with post type filter returns matching`() {
        val posts = listOf(
            Post(type = PostType.OFFER, authorId = "1", authorName = "A", neighborhood = "X", body = "Offering service"),
            Post(type = PostType.REQUEST, authorId = "2", authorName = "B", neighborhood = "Y", body = "Need help"),
            Post(type = PostType.OFFER, authorId = "3", authorName = "C", neighborhood = "Z", body = "Available")
        )

        val results = SemanticSearch.searchPosts(posts, "", null, PostType.REQUEST)

        assertEquals(1, results.size)
        assertEquals(PostType.REQUEST, results[0].type)
    }

    @Test
    fun `searchPosts with combined filters works correctly`() {
        val posts = listOf(
            Post(type = PostType.OFFER, authorId = "1", authorName = "A", neighborhood = "X", body = "Dog walking", serviceType = ServiceType.DOG_WALKING),
            Post(type = PostType.REQUEST, authorId = "2", authorName = "B", neighborhood = "Y", body = "Need dog walker", serviceType = ServiceType.DOG_WALKING),
            Post(type = PostType.OFFER, authorId = "3", authorName = "C", neighborhood = "Z", body = "Tutoring", serviceType = ServiceType.TUTORING)
        )

        val results = SemanticSearch.searchPosts(posts, "dog", ServiceType.DOG_WALKING, PostType.OFFER)

        assertEquals(1, results.size)
        assertEquals("A", results[0].authorName)
    }

    @Test
    fun `searchPosts with skill tags matches correctly`() {
        val posts = listOf(
            Post(
                type = PostType.OFFER,
                authorId = "1",
                authorName = "A",
                neighborhood = "X",
                body = "Dog walking",
                skillTags = listOf("Patient", "CPR Certified")
            ),
            Post(
                type = PostType.OFFER,
                authorId = "2",
                authorName = "B",
                neighborhood = "Y",
                body = "Tutoring",
                skillTags = listOf("Math Expert")
            )
        )

        val score1 = SemanticSearch.matchScore("patient", posts[0])
        val score2 = SemanticSearch.matchScore("patient", posts[1])

        assertTrue(score1 > score2)
    }
}
