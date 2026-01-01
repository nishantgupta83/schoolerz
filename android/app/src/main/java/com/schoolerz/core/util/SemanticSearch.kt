package com.schoolerz.core.util

import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.model.ServiceType

/**
 * Semantic search utility for intelligent post matching
 */
object SemanticSearch {

    private val synonyms = mapOf(
        "dog walking" to listOf("dog walker", "pet care", "walk dog", "walk my dog", "pet sitting", "dog sitter", "walk dogs"),
        "tutoring" to listOf("tutor", "homework help", "study help", "academic help", "math help", "test prep", "SAT prep", "ACT prep"),
        "babysitting" to listOf("babysitter", "childcare", "watch kids", "nanny", "child care", "watch children", "kids care"),
        "lawn care" to listOf("mowing", "yard work", "landscaping", "grass cutting", "lawn mowing", "garden work", "yard cleanup"),
        "tech help" to listOf("computer help", "phone help", "IT support", "fix computer", "tech support", "technology help", "device help"),
        "music lessons" to listOf("music teacher", "piano lessons", "guitar lessons", "instrument lessons", "music tutor", "learn music"),
        "essay review" to listOf("essay help", "writing help", "proofread", "proofreading", "edit essay", "paper review", "editing"),
        "car wash" to listOf("auto detailing", "car cleaning", "wash car", "car detailing", "clean car", "vehicle wash")
    )

    private val offerKeywords = listOf("available", "offering", "can help", "will do", "experienced in", "providing", "i offer", "looking to help")
    private val requestKeywords = listOf("need", "looking for", "searching for", "want", "require", "help wanted", "seeking", "in need of")

    private val serviceMapping = mapOf(
        "dog" to ServiceType.DOG_WALKING,
        "pet" to ServiceType.DOG_WALKING,
        "puppy" to ServiceType.DOG_WALKING,
        "walk" to ServiceType.DOG_WALKING,
        "tutor" to ServiceType.TUTORING,
        "homework" to ServiceType.TUTORING,
        "math" to ServiceType.TUTORING,
        "study" to ServiceType.TUTORING,
        "test" to ServiceType.TUTORING,
        "babysit" to ServiceType.BABYSITTING,
        "child" to ServiceType.BABYSITTING,
        "kids" to ServiceType.BABYSITTING,
        "nanny" to ServiceType.BABYSITTING,
        "lawn" to ServiceType.LAWN_CARE,
        "yard" to ServiceType.LAWN_CARE,
        "mow" to ServiceType.LAWN_CARE,
        "garden" to ServiceType.LAWN_CARE,
        "grass" to ServiceType.LAWN_CARE,
        "tech" to ServiceType.TECH_HELP,
        "computer" to ServiceType.TECH_HELP,
        "phone" to ServiceType.TECH_HELP,
        "laptop" to ServiceType.TECH_HELP,
        "tablet" to ServiceType.TECH_HELP,
        "music" to ServiceType.MUSIC_LESSONS,
        "piano" to ServiceType.MUSIC_LESSONS,
        "guitar" to ServiceType.MUSIC_LESSONS,
        "instrument" to ServiceType.MUSIC_LESSONS,
        "essay" to ServiceType.ESSAY_REVIEW,
        "writing" to ServiceType.ESSAY_REVIEW,
        "proofread" to ServiceType.ESSAY_REVIEW,
        "paper" to ServiceType.ESSAY_REVIEW,
        "car" to ServiceType.CAR_WASH,
        "auto" to ServiceType.CAR_WASH,
        "vehicle" to ServiceType.CAR_WASH,
        "detail" to ServiceType.CAR_WASH
    )

    /**
     * Expand a query with synonyms
     */
    fun expandQuery(query: String): List<String> {
        val lowercased = query.lowercase()
        val expanded = mutableSetOf(lowercased)

        for ((key, values) in synonyms) {
            if (lowercased.contains(key) || values.any { lowercased.contains(it) }) {
                expanded.addAll(values)
                expanded.add(key)
            }
        }

        return expanded.toList()
    }

    /**
     * Detect post type intent from query
     */
    fun detectIntent(query: String): PostType? {
        val lowercased = query.lowercase()

        val hasOfferKeyword = offerKeywords.any { lowercased.contains(it) }
        val hasRequestKeyword = requestKeywords.any { lowercased.contains(it) }

        return when {
            hasOfferKeyword && !hasRequestKeyword -> PostType.OFFER
            hasRequestKeyword && !hasOfferKeyword -> PostType.REQUEST
            else -> null
        }
    }

    /**
     * Detect service type from query
     */
    fun detectServiceType(query: String): ServiceType? {
        val lowercased = query.lowercase()
        val words = lowercased.split(" ")

        for (word in words) {
            serviceMapping[word]?.let { return it }
        }

        return null
    }

    /**
     * Calculate relevance score for a post given a query
     */
    fun matchScore(query: String, post: Post): Double {
        if (query.isBlank()) return 1.0

        var score = 0.0
        val lowercasedQuery = query.lowercase()
        val expandedTerms = expandQuery(query)
        val lowercasedBody = post.body.lowercase()

        // Exact match in body (highest priority)
        if (lowercasedBody.contains(lowercasedQuery)) {
            score += 1.0
        }

        // Synonym matches
        for (term in expandedTerms) {
            if (lowercasedBody.contains(term)) {
                score += 0.7
            }
        }

        // Service type match
        detectServiceType(query)?.let { detectedService ->
            if (post.serviceType == detectedService) {
                score += 0.5
            }
        }

        // Author name match
        if (post.authorName.lowercase().contains(lowercasedQuery)) {
            score += 0.3
        }

        // Neighborhood match
        if (post.neighborhood.lowercase().contains(lowercasedQuery)) {
            score += 0.3
        }

        // Intent match
        detectIntent(query)?.let { detectedIntent ->
            if (detectedIntent == post.type) {
                score += 0.2
            }
        }

        // Skill tags match
        for (tag in post.skillTags) {
            if (tag.lowercase().contains(lowercasedQuery) ||
                expandedTerms.any { tag.lowercase().contains(it) }) {
                score += 0.2
            }
        }

        return score
    }

    /**
     * Rank posts by relevance to query
     */
    fun rankPosts(posts: List<Post>, query: String): List<Post> {
        if (query.isBlank()) return posts

        return posts
            .map { post -> post to matchScore(query, post) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    /**
     * Filter and rank posts based on query and optional filters
     */
    fun searchPosts(
        posts: List<Post>,
        query: String,
        serviceType: ServiceType? = null,
        postType: PostType? = null
    ): List<Post> {
        var filtered = posts

        // Apply service type filter
        serviceType?.let { type ->
            filtered = filtered.filter { it.serviceType == type }
        }

        // Apply post type filter
        postType?.let { type ->
            filtered = filtered.filter { it.type == type }
        }

        // Apply semantic ranking if query exists
        if (query.isNotBlank()) {
            filtered = rankPosts(filtered, query)
        }

        return filtered
    }
}
